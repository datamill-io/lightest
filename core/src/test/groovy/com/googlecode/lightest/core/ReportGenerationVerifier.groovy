package com.googlecode.lightest.core

import static com.googlecode.lightest.core.RunnerSupport.*
import static org.testng.AssertJUnit.*

import com.googlecode.lightest.core.LightestTestCase
import org.testng.annotations.*

class ReportGenerationVerifier extends LightestTestCase {
    public static final METHOD_COUNTS = [ a: 1, b: 1, c: 2, d: 1 ]

    List<String> completedMethods
    File testngResults

    ReportGenerationVerifier() {
        completedMethods = []
        testngResults = new File(OUTPUT_DIR, 'testng-results.xml')
    }

    @AfterClass
    void verifyCompletedMethods() {
        assertEquals(5, completedMethods.size())
    }
    
    private List<File> getResultFiles() {
        def outputDir = new File(OUTPUT_DIR)
        def resultFiles = outputDir.listFiles().findAll {
            ! it.isDirectory() && it.name =~ /test-result-\d+\.xml/
        }
        
        return resultFiles
    }

    /**
     * Given a count of the test methods that have been executed to date, this
     * method verifies the following when the count is non-zero:
     *
     *  1) the correct number of results details were created
     *  2) the result file reports the correct status for each method executed
     *     ("b()" should fail, all others should pass)
     *  3) the result file reports on all expected methods, and only expected
     *     methods.
     *  4) the number of remaining invocation counts for each pending test
     *     method invocation.
     *
     * This method essentially introspects the results generated as it is
     * running!
     */
    private void assertMethodCount() {
        assertEquals(completedMethods.size(), getResultFiles().size())

        def runnerSupport = new RunnerSupport()
            
        completedMethods.each { name ->
            def testResult = runnerSupport.findDetailsByTestMethodName(name)

            if (name == 'b') {
                assertEquals(RunnerTestCase.TEST_FAILED, testResult.'@status')
            }
            else {
                assertEquals(RunnerTestCase.TEST_PASSED, testResult.'@status')
            }
        }

        if (completedMethods.size() == 0) {
            // no testng-results.xml yet
            return
        }
        
        METHOD_COUNTS.each { methodName, count ->
            def completed = completedMethods.count(methodName)
            def pending = count - completed
            def root = new XmlParser().parse(testngResults)
            def node = RunnerTestCase.getPendingTestMethod(root, methodName)
            def pendingAttr = LightestSuiteResultWriter.ATTR_PENDING_INVOCATIONS
            
            //println "methodName: ${methodName}"
            //println "completedMethods: ${completedMethods}"
            //println "completed: ${completed}, pending: ${pending}"
            //println "node: ${node}"

            if (pending > 0) {
                assertEquals(pending, node."@${pendingAttr}".toInteger())
            }
            else {
                assertNull(node)
            }
        }
    }

    @Test
    void a() {
//        assertMethodCount()
        println "a()"
        completedMethods << 'a'
    }

    @Test
    void b() {
        // don't assert here - this method fails in any case, so an assertion
        // failure could not be distinguished
        completedMethods << 'b'
        println "b()"
        fail()
    }

    @Test(invocationCount = 2)
    void c() {
//        assertMethodCount()
        println "c()"
        completedMethods << 'c'
    }

    @Test
    void d() {
//        assertMethodCount()
        println "d()"
        completedMethods << 'd'
    }
}
