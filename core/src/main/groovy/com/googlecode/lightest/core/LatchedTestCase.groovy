package com.googlecode.lightest.core

import java.util.concurrent.CountDownLatch

/**
 * A LightestTestCase associated with a CountDownLatch.*/
class LatchedTestCase {
    LightestTestCase testCase
    CountDownLatch latch

    LatchedTestCase(LightestTestCase testCase, CountDownLatch latch) {
        this.testCase = testCase
        this.latch = latch
    }
}
