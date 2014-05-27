package com.googlecode.lightest.distributed

import com.googlecode.lightest.core.Configuration
import com.googlecode.lightest.core.IConfiguration
import com.googlecode.lightest.core.ITestEnvironment
import com.googlecode.lightest.core.InteractiveTaskDispatchStrategy
import com.googlecode.lightest.core.LightestContextFactory
import com.googlecode.lightest.core.LightestReporter
import com.googlecode.lightest.core.LightestTestListener
import com.googlecode.lightest.core.LightestTestResult
import com.googlecode.lightest.core.LightestUtils
import com.googlecode.lightest.core.ParserFactory
import com.googlecode.lightest.core.TaskDispatcher
import com.googlecode.lightest.core.TestEnvironment
import com.googlecode.lightest.core.TestInstanceFinder
import com.googlecode.lightest.core.TestRegistry
import com.googlecode.lightest.core.ThreadedLightestContext
import com.googlecode.lightest.core.circuitbreaker.CircuitBreaker
import org.testng.ISuite
import org.testng.TestNG
import org.testng.TestNGException
import org.testng.xml.XmlClass
import org.testng.xml.XmlSuite

/**
 * Instance of Test Runner that loads and runs tests on demand. Will not run reports.*/
public class TestRunAgent {
    /** the object used to find test instances, given a prepared TestNG */
    TestInstanceFinder finder

    String outputDir
    String baseDir

    private InputStream _in
    private PrintStream out
    private PrintStream unloadableClassPS
    private ParserFactory parserFactory

    /**
     * if this runner was configured textually, the text will be stored to this
     * variable*/
    private IConfiguration config
    private TestNG testng

    private CircuitBreaker cb
    private LastResultReporter lrr
    List<String> classPaths = []
    List<URL> urls = []
    ITestEnvironment env

    TestRunAgent() {
        this(System.in, System.out)
    }

    TestRunAgent(InputStream _in, PrintStream out) {
        this._in = _in
        this.out = out

        parserFactory = new ParserFactory()
        finder = new TestInstanceFinder()
        lrr = new LastResultReporter()
    }

    static def getLockObject() {
        return TestRunAgent.class
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
        this.config = config

        this.cb = CircuitBreaker.getInstance()
        env = parseEnvironment(config.getEnvironments())
        cb.setEnvMap(env.id)

        config.classPaths.each { path ->
            def file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    classPaths << file.getCanonicalPath()
                } else {
                    urls << file.getCanonicalFile().toURL()
                }
            }
        }
    }

    LightestTestListener makeNewLightestListener(IConfiguration config = new Configuration(), classLoader) {
        def prefs = config.getPreferences()
        def strategy = config.getDispatcherAssignmentStrategy()
        def lightestListener = new LightestTestListener()
        def dispatchStrategy = config.getTaskDispatchStrategy() ?: new InteractiveTaskDispatchStrategy(_in, out)

        def contextClass = config.getContextClass()
        def contextFactory = new LightestContextFactory(contextClass, classLoader, outputDir)
        def context = new ThreadedLightestContext(contextFactory)
        def dispatcher = new TaskDispatcher(context)
        dispatcher.setPreferences(prefs)
        dispatcher.setEnvironment(env)
        dispatcher.setStrategy(dispatchStrategy)
        dispatcher.setListener(lightestListener)

        strategy.setDispatchers(Collections.singletonList(dispatcher))

        lightestListener.registerReporter(lrr)
        lightestListener.setDispatcherAssignmentStrategy(strategy)
        lightestListener.setOutputDir(outputDir)
        return lightestListener
    }

    ITestEnvironment parseEnvironment(List<ITestEnvironment> iTestEnvironments) {
        if (iTestEnvironments || iTestEnvironments.size() > 0){
            return iTestEnvironments[0]
        }
        return new TestEnvironment()
    }

    /**
     * Uses a given class loader to load a Groovy class from a file.
     *
     * @param testFile
     * @param classLoader
     */
    private Class getClassFromFile(File testFile, GroovyClassLoader gcl) {
        if (!testFile.exists()) {
            out.println("Test file not found for test " + testFile.getName())
        }
        assert testFile.exists()

        return gcl.parseClass(testFile.text)
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
                getUnloadableClassPS().println('Skipping unloadable class (syntax?): ' + xmlClass.getName())
                out.println('Skipping unloadable class (syntax?): ' + xmlClass.getName())
            }
        }
    }

    File getGetUnloadableClassPS() {
        if (unloadableClassPS == null) {
            unloadableClassPS = new PrintStream(new FileOutputStream(new File(baseDir, 'unloadableSkippedClasses.txt')))
        }
        return unloadableClassPS
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

    List<LightestTestResult> runTests(ArrayList<String> testClassesToRun) {
        ClassLoader classLoader = makeNewClassLoader()

        def lightestListener = makeNewLightestListener(config, classLoader)

        this.testng = new TestNG()

        List<XmlSuite> suiteFiles = makeSuiteFileFromTests(testClassesToRun, classLoader)

        testng.setUseDefaultListeners(false)
        testng.addListener((Object) lightestListener)
        testng.setOutputDirectory(outputDir)
        testng.setXmlSuites(suiteFiles)

        TestRegistry tr = finder.find(suiteFiles)
        lightestListener.setRegistry(tr)

        testng.run()

        List<LightestTestResult> result = lrr.popResults()

        return result
    }

    private ClassLoader makeNewClassLoader() {
        def classLoader = new GroovyClassLoader()
        Thread.currentThread().setContextClassLoader(classLoader)
        classPaths.each() { path -> classLoader.addClasspath(path); }
        urls.each { url -> classLoader.addURL(url) }
        classLoader
    }

    List<XmlSuite> makeSuiteFileFromTests(List<String> paths, ClassLoader classLoader) {
        ArrayList<Class> classes = []
        paths.each() { path ->
            if (path.endsWith(".groovy")) {
                classes << getClassFromFile(new File(path), classLoader)
            } else {
                classes << getClassFromClassloader(path, classLoader)
            }
        };
        def suitePath = LightestUtils.createSuiteFile(classes, 1, false).getPath()
        def suiteParser = parserFactory.newParser(suitePath)
        return new ArrayList<XmlSuite>(suiteParser.parse())
    }

    Class getClassFromClassloader(String name, ClassLoader classLoader) {
        classLoader.loadClass(name)
    }

    class LastResultReporter extends LightestReporter {
        List<LightestTestResult> results = []

        @Override
        void updateReport(LightestTestResult result, TestRegistry testRegistry,
                          List<XmlSuite> xmlSuites, List<ISuite> suites,
                          String outputDirectory) {
            this.results << result
        }

        List<LightestTestResult> popResults() {
            try {
                return results
            } finally {
                results = []
            }
        }
    }
}
