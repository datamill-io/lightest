package com.googlecode.lightest.core.issue5

import com.googlecode.lightest.core.LightestUtils;
import com.googlecode.lightest.core.RunnerTestCase 

/**
 * Tests that validate issue 5.
 * 
 * See http://code.google.com/p/lightest/issues/detail?id=5)
 */
class Issue5Test extends RunnerTestCase {
    
    void testSubsequentTestRunsWhenAssignmentFailsForPrevious() {
        def configText =
'''
config {
    listeners {
        listener (class: 'com.googlecode.lightest.core.issue5.MethodOrderGuarantor') {
            firstMethodName ('test1')
        }
    }
}
'''
        def testClasses = [ T1.class, T2.class, T3.class ]
        def suiteFile = LightestUtils.createSuiteFile(testClasses)
        def root = runnerSupport.runLightest(suiteFile, configText)
        def methods = getTestMethods(root)
        
        assertEquals(3, methods.size())

        def test1 = getTestMethod(root, 'test1')
        def test2 = getTestMethod(root, 'test2')
        def test3 = getTestMethod(root, 'test3')
        
        def frameworkBefore = getTestOrConfigurationMethod(root, 'frameworkBefore')
        def testSpecificBefore = getTestOrConfigurationMethod(root, 'testSpecificBefore')

        assertNotNull(test1)
        assertEquals(TEST_SKIPPED, test1.'@status')

        assertNotNull(test2)
        assertEquals(TEST_PASSED, test2.'@status')

        assertNotNull(frameworkBefore)
        assertEquals(TEST_PASSED, frameworkBefore.'@status')

        assertNotNull(testSpecificBefore)
        assertEquals(TEST_SKIPPED, testSpecificBefore.'@status')

        assertNotNull(test3)
        assertEquals(TEST_SKIPPED, test3.'@status')
    }
}