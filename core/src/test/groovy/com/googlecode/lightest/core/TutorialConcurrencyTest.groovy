package com.googlecode.lightest.core

import com.googlecode.lightest.core.tutorial.*

class TutorialConcurrencyTest extends RunnerTestCase {
    
    void testConcurrency() {
        def configText =
'''
config {
    envs (class: 'com.googlecode.lightest.core.tutorial.TutorialEnvironment') {
        env (id: 'default') {
            world ('Earth')
        }
        env (id: 'backup') {
            world ('Moon')
        }
    }
}
'''
        def testClasses = [
            TutorialConcurrency1.class,
            TutorialConcurrency2.class,
            TutorialConcurrency3.class,
            TutorialConcurrency4.class
        ]
        def suiteFile = LightestUtils.createSuiteFile(testClasses, 4)
        def root = runnerSupport.runLightest(suiteFile, configText)

        def methods = getTestMethods(root)
        
        assertEquals(200, methods.size())
        
        for (method in methods) {
            assertEquals(TEST_PASSED, method.'@status')
        }
    }
}
