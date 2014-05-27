package com.googlecode.lightest.distributed

import com.googlecode.lightest.core.Configuration
import com.googlecode.lightest.core.IConfiguration
import com.googlecode.lightest.core.LightestTestCase
import com.googlecode.lightest.core.LightestTestResult
import com.googlecode.lightest.core.LightestUtils
import com.googlecode.lightest.core.ParserFactory
import com.googlecode.lightest.core.ReporterInvoker
import com.googlecode.lightest.core.SuiteTracker
import com.googlecode.lightest.core.TestInstanceFinder
import org.testng.TestNGException
import org.testng.xml.XmlClass
import org.testng.xml.XmlSuite

class TestReportAgent {
    List<String> classPaths = []
    List<URL> urls = []
    TestInstanceFinder finder = new TestInstanceFinder()
    ReporterInvoker ri
    String outputDir
    Collection<SuiteTracker> suitesTracked

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
    }

    void configure(IConfiguration config = new Configuration()) {
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
        outputDir = config.getOutputDir()

        ri = new ReporterInvoker()
        ri.registerReporter(config.getXMLReporter())
        ri.registerReporter(config.getPendingReporter())
        ri.registerReporter(config.getFailedReporter())
        config.getReporters().each { ri.registerReporter(it) }
    }

    private ParserFactory parserFactory = new ParserFactory()

    /**
     * Uses a given class loader to load a Groovy class from a file.
     *
     * @param testFile
     * @param classLoader
     */
    private Class getClassFromFile(File testFile, GroovyClassLoader gcl) {
        if (!testFile.exists()) {
            println("Test file not found for test" + testFile.getName())
        }
        assert testFile.exists()
        return gcl.parseClass(testFile.text)
    }

    private Collection<SuiteTracker> makeSuiteTrackers(List<String> paths) {
        GroovyClassLoader classLoader = makeNewClassLoader()
        Thread.currentThread().setContextClassLoader(classLoader)

        ArrayList<XmlSuite> suites = new ArrayList<>()
        ArrayList<String> suitePaths = new ArrayList<>()
        ArrayList<Class> testClasses = new ArrayList<>()
        paths.each { path ->
            if (path.endsWith('.xml')) {
                suitePaths << path
            } else if (path.endsWith('.groovy')) {
                testClasses << getClassFromFile(new File(path), classLoader)
            } else {
                System.out.println "Skipping unrecognized file: ${path}"
            }
        }

        if (testClasses) {
            suitePaths << LightestUtils.createSuiteFile(testClasses, 1).getPath()
        }

        for (suitePath in suitePaths) {
            def parser = parserFactory.newParser(suitePath)
            suites.addAll(parser.parse())
        }

        return finder.makeSuiteTracker(suites)
    }

    private ClassLoader makeNewClassLoader() {
        def classLoader = new GroovyClassLoader()
        classPaths.each() { path -> classLoader.addClasspath(path); }
        urls.each { url -> classLoader.addURL(url) }
        classLoader
    }

    /**
     * Modifies the List of XmlClass'es that is passed in to remove any classes
     * that cannot be loaded using the current Thread's contextClassLoader.
     *
     * @param xmlClasses
     */
    private List<String> findLoadableLightestClassNames(List<XmlClass> xmlClasses) {
        ArrayList<String> loadables = []
        for (xmlClass in xmlClasses) {
            try {
                Class supClass = xmlClass.getSupportClass()
                if (LightestTestCase.class.isAssignableFrom(supClass)) {
                    loadables << xmlClass.name
                } else {
                    println("${supClass} is not a LightestTestCase")
                }
            } catch (TestNGException tnge) {
                println("Skipping unloadable class (syntax?): ${xmlClass.name} (${tnge.message})")
            }
        }
        loadables
    }

    public startSuite(List<String> suiteNames){
        suitesTracked = makeSuiteTrackers(suiteNames)

        suitesTracked.each { ri.generateBaseReport(it.registry, outputDir) }
        suitesTracked.each { ri.updateReport(it.suite, it.registry, outputDir)}
    }

    public testComplete(LightestTestResult ltr, String testName){
        suitesTracked.each {
            it.onTestCompleted(ltr, testName)
            ri.updateReport(ltr, it.registry, [it.xSuite], [it.suite], outputDir)
        }
    }
}
