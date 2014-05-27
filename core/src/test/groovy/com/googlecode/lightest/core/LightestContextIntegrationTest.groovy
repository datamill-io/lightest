package com.googlecode.lightest.core

class LightestContextIntegrationTest extends GroovyTestCase {

    protected void runTestAndAssertSingleSuccess(String testName,
        String configText, String testText)
    {
        def mockTestClass = [ name: testName ]
        def suiteFile = LightestUtils.createSuiteFile([ mockTestClass ])
        def suites = [ suiteFile.getCanonicalPath() ]
        def testRunner = new TestRunner()

        testRunner.configure(configText)
        testRunner.getClassLoader().parseClass(testText)
        testRunner.run(suites, false)

        assertEquals(getOutputDir(), testRunner.getOutputDir())
        
        def resultsFile = new File(getOutputDir(), 'testng-results.xml')
        def parser = RunnerTestCase.getNonValidatingParser()
        def root = parser.parse(resultsFile)
        def testMethods = RunnerTestCase.getTestMethods(root)

        assertEquals(1, testMethods.size())
        assertEquals(RunnerTestCase.TEST_PASSED, testMethods[0].'@status')
    }
    
    protected String getOutputDir() {
        return new File(System.getProperty('java.io.tmpdir'),
            'lightest-report').getCanonicalPath()
    }
    
    void testOutputDirShouldBeSetOnContext() {
        def escapedOutputDir = LightestUtils.backslash(getOutputDir())
        def configText =
"""
config {
    outputDir ('${escapedOutputDir}')
}
"""
        def testText =
"""
import static org.testng.AssertJUnit.*

import com.googlecode.lightest.core.*
import org.testng.annotations.*

class TheTest extends LightestTestCase {
    @Test
    void theTest() {
        // make sure the output dir is available via the context
        assertEquals('${escapedOutputDir}', context.outputDir)
        assertEquals('${escapedOutputDir}', context.getOutputDir())
    }
}
"""

        runTestAndAssertSingleSuccess('TheTest', configText, testText)
    }
}
