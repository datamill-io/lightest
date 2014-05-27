package com.googlecode.lightest.distributed

import com.googlecode.lightest.core.LightestTestResult
import com.googlecode.lightest.distributed.TestRunAgent

class TestRunAgentTest extends GroovyTestCase {
    void testConfigurationAndSuccess(){
        TestRunAgent tra = new TestRunAgent()
        tra.configure(actualConfig.toString())

        List<String> testsToRun= ["src\\test\\groovy\\com\\googlecode\\lightest\\distributed\\SuccessfulTest.groovy"]

        List<LightestTestResult> itr = tra.runTests(testsToRun)
        
        assertNotNull(itr)
        assertEquals(itr.size(), 1)
        assertTrue(itr[0].success)
        assertTrue(itr instanceof Serializable)
    }

    void testConfigurationAndFailure(){
        TestRunAgent tra = new TestRunAgent()
        tra.configure(actualConfig.toString())

        List<String> testsToRun= ["src\\test\\groovy\\com\\googlecode\\lightest\\distributed\\FailureTest.groovy"]

        List<LightestTestResult> itr = tra.runTests(testsToRun)

        assertNotNull(itr)
        assertEquals(itr.size(), 1)
        assertTrue(!itr[0].success)
        assertTrue(itr instanceof Serializable)
    }

    void testConfigurationBackToBack(){
        TestRunAgent tra = new TestRunAgent()
        tra.configure(actualConfig.toString())

        List<String> testsToRun= ["src\\test\\groovy\\com\\googlecode\\lightest\\distributed\\SuccessfulTest.groovy"]

        List<LightestTestResult> itr = tra.runTests(testsToRun)

        assertNotNull(itr)
        assertEquals(itr.size(), 1)
        assertTrue(itr[0].success)
        assertTrue(itr instanceof Serializable)

        testsToRun= ["src\\test\\groovy\\com\\googlecode\\lightest\\distributed\\FailureTest.groovy"]

        itr = tra.runTests(testsToRun)

        assertNotNull(itr)
        assertEquals(itr.size(), 1)
        assertTrue(!itr[0].success)
        assertTrue(itr instanceof Serializable)
    }

    void testConfigurationRelativePath(){
        TestRunAgent tra = new TestRunAgent()
        tra.configure(actualConfig.toString())

        List<String> testsToRun= ["com.googlecode.lightest.distributed.SuccessfulTest"]

        List<LightestTestResult> itr = tra.runTests(testsToRun)

        assertNotNull(itr)
        assertEquals(itr.size(), 1)
        assertTrue(itr[0].success)
        assertTrue(itr instanceof Serializable)
    }

    void testConfigurationAllInOne(){
        TestRunAgent tra = new TestRunAgent()
        tra.configure(actualConfig.toString())

        List<String> testsToRun= ["src\\test\\groovy\\com\\googlecode\\lightest\\distributed\\SuccessfulTest.groovy",
                                  "src\\test\\groovy\\com\\googlecode\\lightest\\distributed\\FailureTest.groovy"]

        List<LightestTestResult> itr = tra.runTests(testsToRun)

        assertNotNull(itr)
        assertEquals(2, itr.size())
        for (test in itr){
            if (test.testClass.name.contains("SuccessfulTest")){
                assertTrue(test.success)
            } else {
                assertTrue(!test.success)
            }
        }
//        ByteArrayOutputStream baos = new ByteArrayOutputStream()
//        ObjectOutputStream oos = new ObjectOutputStream(baos)
//        oos.writeObject(itr)
//        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
//        ObjectInputStream ois = new ObjectInputStream(bais)
//
//        itr = ois.readObject();
    }

def actualConfig = '''def baseDir = binding.variables.baseDir ?: '.'
def username = System.getProperty('user.name')
config {
    classPaths {
        path("src\\\\test\\\\groovy\\\\")
    }
}'''

}
