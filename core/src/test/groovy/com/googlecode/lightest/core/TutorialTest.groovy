package com.googlecode.lightest.core

import com.googlecode.lightest.core.tutorial.*

class TutorialTest extends RunnerTestCase {

    void testTutorial1() {
        def suiteFile = LightestUtils.createSuiteFile([ Tutorial1.class ])
        
        runnerSupport.runLightest(suiteFile)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('sayHello')
        def taskResults = testResult.'task-results'

        assertEquals(TEST_PASSED, testResult.'@status')
        assertEquals(1, taskResults.size())
        assertSuffix('HelloWorld', taskResults[0].'@name')
        assertEquals(TASK_OK, taskResults[0].'@status')
        assertEquals("Said: Hello World!", taskResults[0].'@message')

        testResult = runnerSupport.findDetailsByTestMethodName('sayGreeting')
        taskResults = testResult.'task-results'

        assertEquals(TEST_PASSED, testResult.'@status')
        assertEquals(1, taskResults.size())
        assertSuffix('HelloWorld', taskResults[0].'@name')
        assertEquals(TASK_OK, taskResults[0].'@status')
        assertEquals("Said: Top of the mornin', world!", taskResults[0].'@message')

        assertFalse(getFailedFile().exists())
    }
    
    void testTutorial2() {
        def configText =
'''
config {
    envs (class: 'com.googlecode.lightest.core.tutorial.TutorialEnvironment') {
        env (id: 'default') {
            world ('Earth')
        }
    }
}
'''
        def suiteFile = LightestUtils.createSuiteFile([ Tutorial2.class ])
        
        runnerSupport.runLightest(suiteFile, configText)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('sayGreeting')
        def taskResults = testResult.'task-results'

        assertEquals(TEST_PASSED, testResult.'@status')
        assertEquals(2, taskResults.size())
        assertSuffix('QueryWorld', taskResults[0].'@name')
        assertEquals(TASK_OK, taskResults[0].'@status')
        assertEquals("Current World: Earth", taskResults[0].'@message')
        assertSuffix('HelloWorld', taskResults[1].'@name')
        assertEquals(TASK_OK, taskResults[1].'@status')
        assertEquals("Said: Top of the mornin', world!", taskResults[1].'@message')

        testResult = runnerSupport.findDetailsByTestMethodName('sayGreeting2')
        taskResults = testResult.'task-results'

        assertEquals(TEST_PASSED, testResult.'@status')
        assertEquals(1, taskResults.size())
        assertSuffix('QueryWorld', taskResults[0].'@name')
        assertEquals(TASK_OK, taskResults[0].'@status')
        assertEquals('Find a world, and greet it internationally', taskResults[0].'@description')

        // the child results
        taskResults = taskResults[0].'nested-results'[0].'task-results'

        assertEquals(4, taskResults.size())
        Tutorial2.GREETINGS.eachWithIndex { g, i ->
            assertSuffix('HelloWorld', taskResults[i].'@name')
            assertEquals(TASK_OK, taskResults[1].'@status')
            assertEquals("Said: ${Tutorial2.GREETINGS[i]} (Current World: Earth)", taskResults[i].'@message')
        }

        assertFalse(getFailedFile().exists())
    }

    void testTutorial3() {
        def configText =
'''
config {
    envs (class: 'com.googlecode.lightest.core.tutorial.TutorialEnvironment') {
        env (id: 'default') {
            world ('Alderaan')
        }
    }
}
'''
        def suiteFile = LightestUtils.createSuiteFile([ Tutorial3.class ])
        
        runnerSupport.runLightest(suiteFile, configText)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('sayGreeting')
        def taskResults = testResult.'task-results'
        
        assertEquals(TEST_FAILED, testResult.'@status')
        assertEquals(2, taskResults.size())
        assertSuffix('QueryWorld2', taskResults[0].'@name')
        assertEquals(TASK_FAILED, taskResults[0].'@status')
        assertEquals("Unexpected exception: World not found: Alderaan", taskResults[0].'@message')
        assertPrefix('com.googlecode.lightest.core.tutorial.WorldNotFoundException: World not found: Alderaan', taskResults[0].'detailed-message'[0].text())
        assertEquals(0, taskResults[0].'nested-results'[0].'task-results'.size())
        assertSuffix('HelloWorld', taskResults[1].'@name')
        assertEquals(TASK_OK, taskResults[1].'@status')

        testResult = runnerSupport.findDetailsByTestMethodName('sayHelloBeforeChecking')
        taskResults = testResult.'task-results'

        assertEquals(TEST_FAILED, testResult.'@status')
        assertEquals(1, taskResults.size())
        assertSuffix('HelloWorld', taskResults[0].'@name')
        assertEquals(TASK_FAILED, taskResults[0].'@status')

        // the child results
        taskResults = taskResults[0].'nested-results'[0].'task-results'

        assertEquals(2, taskResults.size())
        assertSuffix('QueryWorld2', taskResults[0].'@name')
        assertEquals(TASK_FAILED, taskResults[0].'@status')
        assertEquals("Unexpected exception: World not found: Alderaan", taskResults[0].'@message')
        assertPrefix('com.googlecode.lightest.core.tutorial.WorldNotFoundException: World not found: Alderaan', taskResults[0].'detailed-message'[0].text())
        assertEquals(0, taskResults[0].'nested-results'[0].'task-results'.size())
        assertSuffix('HelloWorld', taskResults[1].'@name')
        assertEquals(TASK_OK, taskResults[1].'@status')
        assertEquals(0, taskResults[1].'nested-results'[0].'task-results'.size())

        def failedMethods = getFailedMethods()

        assertNotNull(failedMethods.find { it.'@name' == 'sayGreeting' })
        assertNotNull(failedMethods.find { it.'@name' == 'sayHelloBeforeChecking' })
    }

    void testTutorial4() {
        def configText =
'''
config {
    envs (class: 'com.googlecode.lightest.core.tutorial.TutorialEnvironment') {
        env (id: 'default') {
            world ('Hades')
        }
    }
}
'''
        def suiteFile = LightestUtils.createSuiteFile([ Tutorial4.class ])
        
        runnerSupport.runLightest(suiteFile, configText)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('sayHelloBeforeChecking')
        def taskResults = testResult.'task-results'

        assertEquals(TEST_FAILED, testResult.'@status')
        assertEquals(1, taskResults.size())
        assertSuffix('HelloWorld', taskResults[0].'@name')
        assertEquals(TASK_DOOMED, taskResults[0].'@status')

        // the child results
        taskResults = taskResults[0].'nested-results'[0].'task-results'

        assertEquals(1, taskResults.size())
        assertSuffix('QueryWorld3', taskResults[0].'@name')
        assertEquals(TASK_DOOMED, taskResults[0].'@status')
        assertEquals("Unable to find more worlds: Hades", taskResults[0].'@message')
        assertEquals(0, taskResults[0].'nested-results'[0].'task-results'.size())

        def failedMethods = getFailedMethods()

        assertNotNull(failedMethods.find { it.'@name' == 'sayHelloBeforeChecking' })
    }

    void testTutorial5() {
        def configText =
'''
config {
    envs (class: 'com.googlecode.lightest.core.tutorial.TutorialEnvironment') {
        env (id: 'default') {
            world ('Earth')
        }
    }
}
'''
        def suiteFile = LightestUtils.createSuiteFile([ Tutorial5.class ])
        
        runnerSupport.runLightest(suiteFile, configText)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('helloGoodbye')
        def taskResults = testResult.'task-results'

        assertEquals(TEST_PASSED, testResult.'@status')
        assertEquals(1, taskResults.size())
        assertSuffix('GreetWorld', taskResults[0].'@name')
        assertEquals(TASK_OK, taskResults[0].'@status')

        // the child results
        taskResults = taskResults[0].'nested-results'[0].'task-results'

        assertEquals(3, taskResults.size())
        assertSuffix('QueryWorld3', taskResults[0].'@name')
        assertEquals(TASK_OK, taskResults[0].'@status')
        assertSuffix('HelloWorld', taskResults[1].'@name')
        assertEquals(TASK_OK, taskResults[1].'@status')
        assertSuffix('GoodbyeWorld', taskResults[2].'@name')
        assertEquals(TASK_OK, taskResults[2].'@status')

        assertFalse(getFailedFile().exists())
    }

    void testTutorial6() {
        def configText =
'''
config {
    envs (class: 'com.googlecode.lightest.core.tutorial.TutorialEnvironment') {
        env (id: 'default') {
            world ('Alderaan')
        }
    }
}
'''
        def suiteFile = LightestUtils.createSuiteFile([ Tutorial6.class ])
        
        runnerSupport.runLightest(suiteFile, configText)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('helloIsThereAnybodyOutThere')
        def taskResults = testResult.'task-results'

        assertEquals(TEST_FAILED, testResult.'@status')
        assertEquals(1, taskResults.size())
        assertSuffix('GreetWorld', taskResults[0].'@name')
        assertEquals(TASK_FAILED, taskResults[0].'@status')

        // the child results
        taskResults = taskResults[0].'nested-results'[0].'task-results'

        assertEquals(2, taskResults.size())
        assertSuffix('QueryWorld3', taskResults[0].'@name')
        assertEquals(TASK_FAILED, taskResults[0].'@status')
        assertEquals("World not found: Alderaan", taskResults[0].'@message')
        assertEquals(0, taskResults[0].'nested-results'[0].'task-results'.size())
        assertSuffix('HelloWorld', taskResults[1].'@name')
        assertEquals(TASK_OK, taskResults[1].'@status')

        def failedMethods = getFailedMethods()

        assertNotNull(failedMethods.find { it.'@name' == 'helloIsThereAnybodyOutThere' })
    }
    
    void testLocalEnvironmentValues() {
        def suiteFile = LightestUtils.createSuiteFile([ TutorialLocal.class ])
        
        runnerSupport.runLightest(suiteFile)
        
        def testResult = runnerSupport.findDetailsByTestMethodName('testPointlessBureaucracy')
        def taskResults = testResult.'task-results'

        assertEquals(TEST_FAILED, testResult.'@status')
        assertEquals(2, taskResults.size())
        assertSuffix('HelloWorld', taskResults[0].'@name')
        assertEquals(TASK_OK, taskResults[0].'@status')
        assertSuffix('EntrenchedBureaucracy', taskResults[1].'@name')
        assertSuffix(TASK_FAILED, taskResults[1].'@status')
        assertEquals('\$2 to go around...', taskResults[1].'@message')
        
        taskResults = taskResults[1].'nested-results'[0].'task-results'

        assertSuffix('EntrenchedBureaucracy', taskResults[0].'@name')
        assertSuffix(TASK_FAILED, taskResults[0].'@status')
        assertEquals('\$1 to go around...', taskResults[0].'@message')
        
        taskResults = taskResults[0].'nested-results'[0].'task-results'

        assertSuffix('EntrenchedBureaucracy', taskResults[0].'@name')
        assertSuffix(TASK_FAILED, taskResults[0].'@status')
        assertEquals('\$0 to go around...', taskResults[0].'@message')
        
        taskResults = taskResults[0].'nested-results'[0].'task-results'

        assertSuffix('EntrenchedBureaucracy', taskResults[0].'@name')
        assertSuffix(TASK_FAILED, taskResults[0].'@status')
        assertEquals('Ran out of money!', taskResults[0].'@message')
    }
}