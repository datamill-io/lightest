package com.googlecode.lightest.core.issue5

import com.googlecode.lightest.core.ITestEnvironment
import com.googlecode.lightest.core.LightestTestCase
import org.testng.annotations.*

class T3 extends LightestTestCase {
    
    @Override
    boolean canRunIn(ITestEnvironment env) {
        return false
    }
    
    @Override
    Set<String> getFrameworkMethodNames() {
        Set<String> methodNames = super.getFrameworkMethodNames()
        
        methodNames.add('frameworkBefore')

        // original Set not modified
        assert methodNames.size() == super.getFrameworkMethodNames().size() + 1

        return methodNames
    }
    
    @BeforeMethod
    void frameworkBefore() {
        // this configuration method must be run, because it is marked as a
        // framework method by the getConfigurationMethodNames() override
    }
    
    @BeforeMethod
    void testSpecificBefore() {
        // this configuration method must be skipped
    }
    
    @Test
    void test3() {
        // this test must be skipped (due to the above configuration skip)
    }
}