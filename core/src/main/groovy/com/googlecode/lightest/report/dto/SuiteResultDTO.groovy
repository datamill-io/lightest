package com.googlecode.lightest.report.dto

import org.testng.ISuite
import org.testng.ISuiteResult
import org.testng.ITestContext

class SuiteResultDTO implements ISuiteResult, Serializable {
    private static final int serialVersionUID = 1L
    String propertyFileName
    ITestContext testContext

    SuiteResultDTO(ISuiteResult res, ISuite parentSuite){
        this.testContext = new TestContextDTO(res.getTestContext(), parentSuite)
    }

    SuiteResultDTO(String testName, ISuite parentSuite){
        this.testContext = new TestContextDTO(testName, parentSuite)
    }
}
