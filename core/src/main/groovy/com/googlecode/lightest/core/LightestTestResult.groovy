package com.googlecode.lightest.core

import org.testng.ITestResult

/**
 * Wraps a TestNG ITestResult, while linking it to additional Lightest-related
 * fields, such as the list of related ITaskResult's and the ITestEnvironment
 * in which the test was run.*/
class LightestTestResult implements Serializable/* implements ITestResult */ {
    List<ITaskResult> taskResults
    ITestEnvironment env
    int id
    /** In TestNG it goes Suite->Test->Package->TestClass->TestMethod->ParameterSet */
    String testName

    @Delegate
    ITestResult result

    LightestTestResult(ITestResult result, int id) {
        this.result = result
        this.id = id
    }
}
