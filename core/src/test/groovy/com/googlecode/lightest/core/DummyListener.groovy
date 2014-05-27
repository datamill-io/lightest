package com.googlecode.lightest.core

import org.testng.ISuite
import org.testng.ISuiteListener

/**
 * Listener to support unit testing.
 */
class DummyListener implements ISuiteListener {
    String someProperty
    boolean onFinishWasCalled = false
    boolean onStartWasCalled = false
    
    void onFinish(ISuite suite) {
        onFinishWasCalled = true
    }
    
    void onStart(ISuite suite) {
        onStartWasCalled = true
    }
}
