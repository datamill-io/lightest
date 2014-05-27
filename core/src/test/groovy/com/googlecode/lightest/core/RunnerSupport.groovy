package com.googlecode.lightest.core

import org.testng.ITestNGListener

class RunnerSupport {
    public static final String TEMP_DIR = System.getProperty('java.io.tmpdir')
    public static final String OUTPUT_DIR =
        new File(TEMP_DIR, 'lightest-report').getPath()
    public static final String RESULTS_XML = 'testng-results.xml'
    public static final String FAILED_XML = 'testng-failed.xml'

    TestRunner testRunner
    List<ITestNGListener> testNGListeners

    RunnerSupport() {
        testRunner = new TestRunner()
        testNGListeners = []
    }

    /**
     * Runs the suite specified by a resource path, using the given,
     * configuration, and returns the testng-results.xml file.
     *
     * @param suitePath
     * @param configText
     */
    Node runLightest(String suitePath, String configText = "") {
        def suiteStream = this.class.getResourceAsStream(suitePath)
        def suiteFile = createSuiteFileFromStream(suiteStream)
        
        return runLightest(suiteFile, configText)
    }

    /**
     * Runs the suite specified by a suite XML file, using the given,
     * configuration, and returns the Node representation of the details XML
     * file. The output directory is always overridden to be OUTPUT_DIR.
     *
     * @param suiteFile
     * @param configText
     */
    Node runLightest(File suiteFile, configText = "") {
        def suites = [ suiteFile.getCanonicalPath() ]

        testRunner.configure(configText)
        testRunner.setOutputDir(OUTPUT_DIR)
        
        // add the listeners after the configuration step
        testNGListeners.each { testRunner.testNGListeners << it }
        
        testRunner.run(suites, false)

        def resultsFile = new File(testRunner.getOutputDir(), RESULTS_XML)

        return new XmlParser().parse(resultsFile)
    }
    
    /**
     * Returns the root Node of the XML details file for the given test method,
     * or null if it can't be found.
     * 
     * @param methodName
     */
    Node findDetailsByTestMethodName(String methodName) {
        for (file in new File(OUTPUT_DIR).listFiles()) {
            if (file.isDirectory()) {
                continue
            }
            if (! file.name.endsWith('.xml')) {
                continue
            }
            
            def root = new XmlParser().parse(file)
            
            if (root.'@name' == methodName) {
                return root
            }
        }
        
        return null
    }

    /**
     * Creates and returns a temporary file using the contents of an input
     * stream. The file will be deleted when the JVM exits.
     *
     * @param stream  the stream whose contents to write to the file
     */
    private createSuiteFileFromStream(stream) {
        assert stream != null

        def file = File.createTempFile('lightest-suite', '.xml')
        file.deleteOnExit()
        file.write(stream.text)

        return file
    }
}