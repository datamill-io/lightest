package com.googlecode.lightest.core

import com.googlecode.lightest.core.tutorial.Tutorial1
import com.googlecode.lightest.core.tutorial.Tutorial4

import org.testng.reporters.XMLReporterConfig

class RunnerTest extends RunnerTestCase {

    /**
     * Tests that a run results are updated as each test method is run. The
     * test class itself contains assertions that will cause the assertions
     * in this method to fail, should the methods in the test class fail. Yes,
     * it's a little confusing!
     */
    void testIntermediateReportGeneration() {
        def testClass = ReportGenerationVerifier.class
        def suiteFile = LightestUtils.createSuiteFile([ testClass ])
        def configText =
'''
config {
    reporters {
        reporter (class: 'com.googlecode.lightest.core.XMLReporter', role: 'XMLReporter') {
            // disable scheduling
            updateEnabled (true)
            scheduled     (false)
        }
        reporter (class: 'com.googlecode.lightest.core.DefaultDetailsReporter')
    }
}
'''
        println(suiteFile.text)
        
        runnerSupport.runLightest(suiteFile, configText)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('a')

        assertEquals(TEST_PASSED, testResult.'@status')
        
        testResult = runnerSupport.findDetailsByTestMethodName('b')
        
        assertEquals(TEST_FAILED, testResult.'@status')
        
        testResult = runnerSupport.findDetailsByTestMethodName('c')
        
        assertEquals(TEST_PASSED, testResult.'@status')
        
        testResult = runnerSupport.findDetailsByTestMethodName('d')
        
        assertEquals(TEST_PASSED, testResult.'@status')
    }
    
    void testExtraListenerIsRegisteredWithTestNG() {
        def testClass = Tutorial1.class
        def suiteFile = LightestUtils.createSuiteFile([ testClass ])
        def listener = new DummyListener()
        
        assertEquals(false, listener.onStartWasCalled)
        assertEquals(false, listener.onFinishWasCalled)
        
        runnerSupport.testNGListeners << listener
        runnerSupport.runLightest(suiteFile)
        
        assertEquals(true, listener.onStartWasCalled)
        assertEquals(true, listener.onFinishWasCalled)
    }
    
    /**
     * The skip logic has been removed for now, thus this test isn't applicable.
     */
    void testFailedDispatcherAssignmentCausesConfigurationSkips() {
        def testClass = Tutorial4.class
        def suiteFile = LightestUtils.createSuiteFile([ testClass ])
        def configText =
'''
config {
    dispatcherAssignmentStrategy (class: 'com.googlecode.lightest.core.NonAssigningDispatcherAssignmentStrategy')
}
'''
        
        def root = runnerSupport.runLightest(suiteFile, configText)
        def assign = getTestOrConfigurationMethod(root, 'assign')
        def setUp = getTestOrConfigurationMethod(root, 'setUp')
        def sayHelloBeforeChecking = getTestOrConfigurationMethod(root,
            'sayHelloBeforeChecking')
        def tearDown = getTestOrConfigurationMethod(root, 'tearDown')
        def unassign = getTestOrConfigurationMethod(root, 'unassign')
        
        assertEquals(5, getTestOrConfigurationMethods(root).size())
        assertEquals(XMLReporterConfig.TEST_PASSED, assign.'@status')
        assertEquals(XMLReporterConfig.TEST_PASSED, setUp.'@status')
        assertEquals(XMLReporterConfig.TEST_SKIPPED, sayHelloBeforeChecking.'@status')
        assertEquals(XMLReporterConfig.TEST_PASSED, tearDown.'@status')
        assertEquals(XMLReporterConfig.TEST_PASSED, unassign.'@status')
    }
}