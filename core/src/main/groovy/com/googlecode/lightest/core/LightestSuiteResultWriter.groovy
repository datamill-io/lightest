package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestInstance
import com.googlecode.lightest.core.TestRegistry

import java.text.SimpleDateFormat

import org.testng.ISuiteResult
import org.testng.ITestResult
import org.testng.reporters.XMLReporterConfig
import org.testng.reporters.XMLStringBuffer

class LightestSuiteResultWriter extends XMLSuiteResultWriter
        implements ITestRegistryAcceptor {

    public static final String TAG_TEST_PENDING = 'test-pending'
    public static final String ATTR_PENDING_INVOCATIONS = 'pending-invocations'

    TestRegistry registry

    LightestSuiteResultWriter() {
        this(null)
    }

    LightestSuiteResultWriter(XMLReporterConfig config) {
        super(config)
    }

    /**
     * Except for a small modification, this is a clone of the parent class'
     * implementation, and should be kept in sync with it when upgrading the
     * TestNG version.*/
    @Override
    protected void writeAllToBuffer(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
        xmlBuffer.push(XMLReporterConfig.TAG_TEST, getSuiteResultAttributes(suiteResult));
        Set<ITestResult> testResults = new HashSet();
        addAllTestResults(testResults, suiteResult.getTestContext().getPassedTests());
        addAllTestResults(testResults, suiteResult.getTestContext().getFailedTests());
        addAllTestResults(testResults, suiteResult.getTestContext().getSkippedTests());
        addAllTestResults(testResults, suiteResult.getTestContext().getPassedConfigurations());
        addAllTestResults(testResults, suiteResult.getTestContext().getSkippedConfigurations());
        addAllTestResults(testResults, suiteResult.getTestContext().getFailedConfigurations());
        addAllTestResults(testResults, suiteResult.getTestContext().getFailedButWithinSuccessPercentageTests());
        // HBC - key change here (new method signature)
        addTestResults(xmlBuffer, testResults,
                       registry.match(suiteResult.testContext.suite.name, suiteResult.testContext.name));
        // HBC - end key change
        xmlBuffer.pop();
    }

    @Override
    protected Properties getSuiteResultAttributes(ISuiteResult suiteResult) {
        def attrs = super.getSuiteResultAttributes(suiteResult)
        def testContext = suiteResult.getTestContext()
        def dateFormat = new SimpleDateFormat(config.getTimestampFormat())
        def startDate = testContext.getStartDate()
        def endDate = testContext.getEndDate()

        attrs.setProperty(XMLReporterConfig.ATTR_STARTED_AT, dateFormat.format(startDate))

        if (endDate) {
            attrs.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, dateFormat.format(endDate))
            attrs.setProperty(XMLReporterConfig.ATTR_DURATION_MS, "${endDate.getTime() - startDate.getTime()}")
        }

        return attrs
    }

    protected void addTestResults(XMLStringBuffer xmlBuffer, Set<ITestResult> testResults, TestRegistry registryByTest) {
        assert registryByTest != null

        // the key is the class name
        Map<String, List<ITestResult>> testsGroupedByClass = buildTestClassGroups(testResults)

        registryByTest.getUniqueClassNames().each { className ->
            def attrs = new Properties()
            def testResultsByClass = testsGroupedByClass[className]
            def registryByClass = registryByTest.match(className)

            if (config.isSplitClassAndPackageNames()) {
                int dot = className.lastIndexOf('.')
                attrs.setProperty(XMLReporterConfig.ATTR_NAME,
                                  dot > -1 ? className.substring(dot + 1) : className)
                attrs.setProperty(XMLReporterConfig.ATTR_PACKAGE,
                                  dot > -1 ? className.substring(0, dot) : "[default]")
            } else {
                attrs.setProperty(XMLReporterConfig.ATTR_NAME, className)
            }

            xmlBuffer.push(XMLReporterConfig.TAG_CLASS, attrs)

            if (testResultsByClass != null) {
                // results with no corresponding registry entry are removed
                def sortedResults = testResultsByClass.findAll {
                    !it.getMethod().isTest() || registry.getId(it) != null
                }

                sortedResults.sort()

                for (ITestResult testResult : sortedResults) {
                    addTestResult(xmlBuffer, testResult, registry)
                }
            }

            registryByClass.data.each { testInstance, pendingCount ->
                if (pendingCount > 0) {
                    addPendingTest(xmlBuffer, testInstance, pendingCount)
                }
            }

            xmlBuffer.pop()
        }
    }

    protected void addTestResult(XMLStringBuffer xmlBuffer,
                                 ITestResult testResult, TestRegistry registry) {
        def attributes = getTestResultAttributes(testResult)
        def status = getStatusString(testResult.getStatus())

        attributes.setProperty(XMLReporterConfig.ATTR_STATUS, status)

        if (testResult.getMethod().isTest()) {
            attributes.setProperty('id', "${registry.getId(testResult)}")
        }

        xmlBuffer.push(XMLReporterConfig.TAG_TEST_METHOD, attributes)

        addTestMethodParams(xmlBuffer, testResult)
        addTestResultException(xmlBuffer, testResult)

        xmlBuffer.pop()
    }

    protected void addPendingTest(XMLStringBuffer xmlBuffer,
                                  TestInstance testInstance, int pendingCount) {
        def attrs = getPendingTestAttributes(testInstance, pendingCount)
        xmlBuffer.addEmptyElement(TAG_TEST_PENDING, attrs)
    }

    protected Properties getPendingTestAttributes(TestInstance testInstance, int pendingCount) {
        def attrs = new Properties()

        attrs.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, testInstance.methodSignature)
        attrs.setProperty(XMLReporterConfig.ATTR_NAME, testInstance.methodName)
        attrs.setProperty(ATTR_PENDING_INVOCATIONS, "${pendingCount}")

        return attrs
    }
}