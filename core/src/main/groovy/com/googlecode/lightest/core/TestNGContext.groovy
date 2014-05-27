package com.googlecode.lightest.core

import org.testng.ITestContext

/**
 * The context of a TestNG test run, from the perspective of a TestListener.*/
class TestNGContext {
    String suiteName
    String testName

    TestNGContext(ITestContext context) {
        this.suiteName = context.suite.name
        this.testName = context.name
    }
}

