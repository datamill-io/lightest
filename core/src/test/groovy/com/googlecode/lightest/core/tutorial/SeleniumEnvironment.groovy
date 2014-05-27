package com.googlecode.lightest.core.tutorial

import com.googlecode.lightest.core.ITestEnvironment
import com.googlecode.lightest.core.TestEnvironment

class SeleniumEnvironment extends TestEnvironment implements ITestEnvironment {
    String seleniumServerHost
    int seleniumServerPort
    String defaultBrowserStartCommand
}