package com.googlecode.lightest.core

import com.googlecode.lightest.core.circuitbreaker.CircuitBreaker
import com.googlecode.lightest.core.TestInstanceFinder
import org.apache.commons.cli.GnuParser
import org.codehaus.groovy.runtime.StackTraceUtils
import org.testng.ITestNGListener
import org.testng.TestNG
import org.testng.TestNGException
import org.testng.xml.XmlClass
import org.testng.xml.XmlPackage
import org.testng.xml.XmlSuite
import org.testng.xml.XmlTest

/**
 * The Lightest test runner.*/
class TestRunner implements ITestRunner {
    /** the object used to find test instances, given a prepared TestNG */
    TestInstanceFinder finder

    /**
     * the ITestNGListener's to wire directly into the TestNG runner. Any
     * existing values will be overwritten if configure() is called afterwards.*/
    List<ITestNGListener> testNGListeners

    String outputDir
    String baseDir

    private InputStream _in
    private PrintStream out
    private PrintStream unloadableClassPS
    private AntBuilder ant
    private ParserFactory parserFactory
    private GroovyClassLoader classLoader
    private ILightestTestListener lightestListener
    private List<ITaskDispatcher> dispatchers
    private ITaskDispatchStrategy dispatchStrategy
    private boolean interactiveThreadShouldStop

    private int threadCount = 1
    /**
     * if this runner was configured textually, the text will be stored to this
     * variable*/
    private String configText
    private IConfiguration config
    private TestNG testng

    private CircuitBreaker cb

    TestRunner() {
        this(System.in, System.out)
    }

    TestRunner(InputStream _in, PrintStream out) {
        this(_in, out, new AntBuilder())
    }

    TestRunner(InputStream _in, PrintStream out, AntBuilder ant) {
        this._in = _in
        this.out = out
        this.ant = ant

        parserFactory = new ParserFactory()
        testNGListeners = []
        finder = new TestInstanceFinder()
        classLoader = new GroovyClassLoader(this.class.classLoader)
        dispatchStrategy = new InteractiveTaskDispatchStrategy(_in, out)
    }

    static def getLockObject() {
        return TestRunner.class
    }
/**
 * Configures the runner by wiring together dependencies as specified by
 * the configuration file.
 *
 * @param configFile
 */
    void configure(File configFile) {
        configure(configFile.text)
    }

    /**
     * Configures the runner by wiring together dependencies as specified by
     * the configuration text.
     *
     * @param configText
     */
    void configure(String configText) {
        def config = new Configuration()

        config.init(configText)
        configure(config)
        this.configText = configText
        this.config = config
    }

    void configure(IConfiguration config = new Configuration()) {
        this.cb = CircuitBreaker.getInstance()
        def classPaths = config.getClassPaths()
        def prefs = config.getPreferences()
        def envs = config.getEnvironments()
        def strategy = config.getDispatcherAssignmentStrategy()
        def reporters = config.getReporters()
        lightestListener = new LightestTestListener()
        testNGListeners = config.getListeners()
        dispatchStrategy = config.getTaskDispatchStrategy() ?: dispatchStrategy
        dispatchers = []

        lightestListener.registerReporter(config.getXMLReporter())
        lightestListener.registerReporter(config.getPendingReporter())
        lightestListener.registerReporter(config.getFailedReporter())
        initThreadCount(config)
        reporters.each { reporter -> lightestListener.registerReporter(reporter)
        }

        outputDir = config.getOutputDir()
        classLoader = config.getClassLoader()

        classPaths.each { path ->
            def file = new File(path);
            if (file.exists()) {
                parseFileForClassLoaderUrls(file, classLoader);
            }
        }

        envs.each { env ->
            def contextClass = config.getContextClass()
            def contextFactory = new LightestContextFactory(contextClass,
                                                            classLoader, outputDir)
            def context = new ThreadedLightestContext(contextFactory)
            def dispatcher = new TaskDispatcher(context)

            dispatcher.setPreferences(prefs)
            dispatcher.setEnvironment(env)
            dispatcher.setStrategy(dispatchStrategy)
            dispatcher.setListener(lightestListener)

            cb.setEnvMap(env.id)

            dispatchers << dispatcher
        }

        strategy.setDispatchers(dispatchers)
        lightestListener.setDispatcherAssignmentStrategy(strategy)

        //initialize the secondary printstream we created
        baseDir = outputDir
        baseDir = baseDir.substring(0, baseDir.lastIndexOf(File.separator) + 1)
        unloadableClassPS = new PrintStream(new FileOutputStream(new File(baseDir, 'unloadableSkippedClasses.txt')))

    }

    void parseFileForClassLoaderUrls(File file, GroovyClassLoader classLoader) {
        if (file.directory) {
            classLoader.addClasspath(file.getCanonicalPath());
        } else {
            URL url = file.getCanonicalFile().toURL()
            classLoader.addURL(url)
        }
    }

    /**
     * Returns the current GroovyClassLoader, which may be used by TestNG to
     * load test and related classes. The underlying object may change if
     * configure() is invoked.*/
    GroovyClassLoader getClassLoader() {
        return classLoader
    }

    /**
     * Uses a given class loader to load a Groovy class from a file.
     *
     * @param testFile
     * @param classLoader
     */
    private Class getClassFromFile(File testFile, GroovyClassLoader gcl) {
        if (!testFile.exists()) {
            out.println("Test file not found for test" + testFile.getName())
        }
        assert testFile.exists()
        return gcl.parseClass(testFile.text)
    }

    /**
     * Returns a List of XmlSuite's based on parsing the suite XML files found
     * at the given paths. All references to unloadable classes are reported
     * on to the console and removed.
     *
     * @param paths a List of String's representing paths to the suite XML
     *               files to return as XmlSuite's and/or specific .groovy
     *               test files
     */
    private List<XmlSuite> getXmlSuites(List<String> paths) {
        ArrayList<XmlSuite> suites = new ArrayList<>()
        ArrayList<String> suitePaths = new ArrayList<>()
        ArrayList<Class> testClasses = new ArrayList<>()
        paths.each { path ->
            if (path.endsWith('.xml')) {
                suitePaths << path
            } else if (path.endsWith('.groovy')) {
                testClasses << getClassFromFile(new File(path), classLoader)
            } else {
                out.println "Skipping unrecognized file: ${path}"
            }
        }

        if (testClasses) {
            suitePaths << LightestUtils.createSuiteFile(testClasses, threadCount).getPath()
        }

        for (suitePath in suitePaths) {
            def parser = parserFactory.newParser(suitePath)
            suites.addAll(parser.parse())
        }

        for (XmlSuite suite in suites) {
            for (XmlTest test in suite.getTests()) {
                removeUnloadableClasses(test.getXmlClasses())

                for (XmlPackage xmlPackage in (test.getXmlPackages() ?: [])) {
                    removeUnloadableClasses(xmlPackage.getXmlClasses())
                }
            }
        }

        //close the unloadableClassPS printstream we created
        if (unloadableClassPS != null) unloadableClassPS.close()

        return suites
    }

    /**
     * Modifies the List of XmlClass'es that is passed in to remove any classes
     * that cannot be loaded using the current Thread's contextClassLoader.
     *
     * @param xmlClasses
     */
    private void removeUnloadableClasses(List<XmlClass> xmlClasses) {
        def i = 0

        while (i < xmlClasses.size()) {
            def xmlClass = xmlClasses[i]

            try {
                xmlClass.getSupportClass()
                ++i
            } catch (TestNGException tnge) {
                // "Cannot find class in classpath: ...", so remove the class
                // from the list
                xmlClasses.remove(i)
                unloadableClassPS.println('Skipping unloadable class (syntax?): ' + xmlClass.getName())
                out.println('Skipping unloadable class (syntax?): ' + xmlClass.getName())
            }
        }
    }

    /**
     * Executes the test suites represented by XML files, along with tests
     * represented as Groovy files, whose paths are provided as a parameter to
     * this method. The contextual class loader of the TestNG run is a
     * GroovyClassLoader that is aware of any task classpaths specified in the
     * Configuration.
     *
     * @param paths paths to files that are either Lightest suite
     *                          XML files, or Groovy scripts that are
     *                          LightestTestCase's.
     * @param interactiveStart whether to start the runner in interactive
     *                          mode, if possible
     */
    void run(List<String> paths, boolean interactiveStart) {
        // the output directory is set on all appropriate collaborating classes
        // in this method, so they don't get out of sync
        assert outputDir != null

        // all class loading performed by TestNG should be done via this
        // loader. Everything having to do with the new TestNG object is
        // confined to execute after the context class loader has been set
        // here, for that reason.
        Thread.currentThread().setContextClassLoader(classLoader)

        this.testng = new TestNG()
        List<XmlSuite> suites = getXmlSuites(paths)

        boolean runValid = true;
        config.getRunValidators().each {
            if (!it.validate(config, suites)) {
                runValid = false
            }
        }
        if (!runValid) {
            return
        }

        testng.setUseDefaultListeners(false)
        testng.addListener((Object) lightestListener)
        testng.setOutputDirectory(outputDir)
        testng.setXmlSuites(suites)

        testNGListeners.each { testng.addListener((Object) it) }

        def duplicates = getDuplicateSuiteNames(testng)

        if (duplicates.size() > 0) {
            out.println("Duplicate suite names are not allowed: ${duplicates}")
            return
        }

        // the old report directory should be cleaned before the reporters are
        // initialized
        ant.delete(dir: outputDir, failonerror: false)

        lightestListener.setRegistry(finder.find(suites))
        lightestListener.setOutputDir(outputDir)
        lightestListener.initializeReporters(configText)

        //copy the unloadableSkippedClasses.txt file to the newly emptied lightest-report directory
        File originalFile = new File(baseDir, 'unloadableSkippedClasses.txt')
        originalFile.renameTo(new File(outputDir, originalFile.getName()))

        if (dispatchStrategy instanceof IInterruptibleTaskDispatchStrategy) {
            def isParallel = testng.m_suites.any {
                it.getParallel() == XmlSuite.PARALLEL_TESTS || it.getParallel() == XmlSuite.PARALLEL_METHODS
            }

            if (isParallel) {
                dispatchStrategy.setInterruptible(false)
            } else if (interactiveStart) {
                interrupt()
            }
        }

        testng.run()
    }

    protected Set<String> getDuplicateSuiteNames(TestNG testng) {
        def duplicates = new HashSet<String>()
        def all = new HashSet<String>()
        def suiteNames = testng.m_suites.collect { it.getName() }

        for (suiteName in suiteNames) {
            if (all.contains(suiteName)) {
                duplicates << suiteName
            }

            all << suiteName
        }

        return duplicates
    }

    /**
     * Starts a thread that reads the runner's input stream for a directive to
     * start interactive mode. This is typically whenever the user hits the
     * Enter/Return key. The content of the line entered will be discarded.*/
    protected void startInteractiveThread() {
        def interactiveThread = [run: {
            def reader = _in.newReader()

            while (!interactiveThreadShouldStop) {
                if (reader.ready()) {
                    reader.readLine()
                    interrupt()
                }

                sleep 100
            }
        }] as Thread

        interactiveThreadShouldStop = false
        interactiveThread.start()
    }

    /**
     * Instructs the input scanning thread to stop at the next convenient time.
     * If the thread is not currently running, this is a no-op.*/
    protected void stopInteractiveThread() {
        interactiveThreadShouldStop = true
    }

    /**
     * Prepares to enter interactive mode in response to user input.
     * Interactive mode cannot be entered when tests are being run in parallel.
     * We can only know whether tests are being run in parallel after the
     * TestNG suites have been set, e.g. after setSuites() has been called.
     * This method will fail with an assertion error if called beforehand. If
     * the current task dispatch strategy does not allow for interruption, this
     * will be a no-op.*/
    protected void interrupt() {
        if (dispatchStrategy instanceof IInterruptibleTaskDispatchStrategy) {
            if (dispatchStrategy.isInterruptible()) {
                dispatchStrategy.interrupt()
            } else {
                out.println('[interruption not allowed]')
            }
        }
    }

    static void main(args) {
        final USAGE = ('java -jar lightest-core-standalone.jar ' + '[OPTIONS] CONFIGFILE [TESTFILE | SUITEFILE]...')

        def cli = new CliBuilder(usage: USAGE, parser: new GnuParser())

        cli.i(longOpt: 'interactive', required: false,
              'start in interactive mode')
        cli.b(longOpt: 'batch', required: false,
              'batch mode (disables interactive mode)')

        def options = cli.parse(args)
        def argsList = options.getArgs().toList()

        if (argsList.size() < 2) {
            cli.usage()
            return
        }

        def testRunner = new TestRunner()

        testRunner.configure(new File(argsList.remove(0)))

        try {
            if (!options.b) {
                testRunner.startInteractiveThread()
                testRunner.run(argsList, options.i)
                testRunner.stopInteractiveThread()
            } else {
                testRunner.run(argsList, false)
            }
            if (testRunner.testng.hasFailure()) {
                System.exit(1)
            } else {
                System.exit(0)
            }
        } catch (Throwable e) {
            StackTraceUtils.deepSanitize(e)
            e.printStackTrace()
            testRunner.stopInteractiveThread()
            System.exit(1)
        }
    }

    private void initThreadCount(IConfiguration config) {
        def prefs = config.getPreferences()
        def envs = config.getEnvironments()
        if (prefs == null) {
            return
        }
        try {
            threadCount = (prefs.getThreadCount() > 1) ? prefs.getThreadCount() : 1
            if (threadCount > envs.size()) {
                out.println(
                        "Thread-count of ${threadCount} in config is greater than environments configured, changing thread-count to max environments,${envs.size()}")
                threadCount = envs.size()
            }
        } catch (Exception e) {
            threadCount = 1
        }
    }
}
