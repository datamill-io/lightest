package com.googlecode.lightest.core

import com.googlecode.lightest.core.LightestTestResult
import com.googlecode.lightest.report.dto.SuiteDTO
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * Given events about a suite, track them using objects compatible with TestNG integrated reporting.
 */
class SuiteTracker {
    TestRegistry registry
    SuiteDTO suite
    XmlSuite xSuite
    SuiteTracker(TestRegistry tr, XmlSuite xs, ISuite s){
        this.registry = tr;
        this.xSuite = xs
        this.suite = new SuiteDTO(s, xSuite.name)
        suite.xmlSuite = xSuite
    }

    void onTestCompleted(LightestTestResult ltr, String testName){
        registry.resolve(suite.name, testName, ltr.result, ltr.id)
        registry.register(ltr.env, ltr.result)

        suite.addResult(ltr.result, testName)
        if (registry.wereAllTestsRun(testName)){
            suite.completed(testName, new Date(ltr.endMillis))
        }
    }
}
