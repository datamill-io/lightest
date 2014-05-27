package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestInstance
import com.googlecode.lightest.core.TestRegistry
import org.testng.ISuite
import org.testng.xml.XmlClass
import org.testng.xml.XmlSuite
import org.testng.xml.XmlTest

/**
 * Maintains an XML file that represents the tests that have not yet been run.
 * This reporter may be invoked throughout the test suite execution.*/
class PendingReporter extends LightestReporter {
    /** the className of the generated file */
    public static final String TESTNG_PENDING_XML = 'testng-pending.xml'

    /**
     * If there are any pending tests, an XML suite file representing those
     * tests, by class, is created in the test suite's output directory.
     * Otherwise, any existing pending file is deleted. This is done for every
     * suite.
     *
     * @param result not used
     * @param xmlSuites
     * @param suites
     * @param outputDirectory
     */
    @Override
    void updateReport(LightestTestResult result, TestRegistry registry,
                      List<XmlSuite> xmlSuites, List<ISuite> suites,
                      String outputDirectory) {

        for (i in 0..<xmlSuites.size()) {
            def xmlSuite = xmlSuites[i]
            def suite = suites[i]
            def pendingSuite = xmlSuite.clone()
            def outputDir = new File(outputDirectory)
            def pendingFile = new File(outputDir, TESTNG_PENDING_XML)

            for (XmlTest xmlTest in xmlSuite.getTests()) {
                def testRegistry = registry.match(xmlSuite.getName(),
                                                  xmlTest.getName())
                def testInstances = (testRegistry.getData().findAll {
                    it.value > 0
                }.keySet())
                def methodsByClass = getMethodsByClass(testInstances)
                def xmlClasses = []

                methodsByClass.each { className, methods ->
                    def xmlClass = new XmlClass(className)

                    xmlClass.setIncludedMethods(methods)
                    xmlClasses << xmlClass
                }

                if (xmlClasses.size() > 0) {
                    def pendingTest = new XmlTest(pendingSuite)

                    copyAttributes(xmlTest, pendingTest)
                    pendingTest.setXmlClasses(xmlClasses)
                }
            }

            if (pendingSuite.getTests().size() > 0) {
                outputDir.mkdirs()
                pendingFile.text = pendingSuite.toXml()
            } else {
                if (pendingFile.exists()) {
                    pendingFile.delete()
                }
            }
        }
    }

    protected Map<String, List<String>> getMethodsByClass(Collection<TestInstance> testInstances) {
        def methodsByClass = [:]

        testInstances.each { testInstance ->
            def className = testInstance.className

            if (!methodsByClass[className]) {
                methodsByClass[className] = []
            }

            methodsByClass[className] << testInstance.methodName
        }

        return methodsByClass
    }

    /**
     * @see org.testng.reporters.FailedReporter#createXmlTest
     */
    protected void copyAttributes(XmlTest source, XmlTest target) {
        target.setName(source.getName() + '(pending)')
        target.setAnnotations(source.getAnnotations())
        target.setBeanShellExpression(source.getExpression())
        target.setIncludedGroups(source.getIncludedGroups())
        target.setExcludedGroups(source.getExcludedGroups())
        target.setParallel(source.getParallel())
        target.setParameters(source.getParameters())
        target.setJUnit(source.isJUnit())
    }
}    
