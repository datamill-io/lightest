package com.googlecode.lightest.core.issue5

import com.googlecode.lightest.core.ITestEnvironment
import com.googlecode.lightest.core.LightestTestCase
import org.testng.annotations.*

// don't name class "Test*", else the maven plugin runs it as a test!
class T1 extends LightestTestCase {
    
    @Override
    boolean canRunIn(ITestEnvironment env) {
        return false
    }
    
    @Test
    void test1() {
        // this test will be skipped
    }
}