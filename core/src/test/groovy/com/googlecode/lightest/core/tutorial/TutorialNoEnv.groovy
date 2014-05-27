package com.googlecode.lightest.core.tutorial

import org.testng.annotations.Test;

import com.googlecode.lightest.core.ITestEnvironment;

/**
 * A test that doesn't support any environment.  This allows testing the
 * situation where you run a test which has environment restrictions,
 * but your configuration doesn't include a matching environment.
 */
class TutorialNoEnv extends TutorialBase {
    @Test
    void sayHello() {
        HelloWorld ()
    }
    
    @Test
    void sayGreeting() {
        HelloWorld (greeting: "Top of the mornin', world!")
    }
    
    @Override
    public boolean canRunIn(ITestEnvironment env) {
        return false
    }
}
