package com.googlecode.lightest.report.dto

import org.testng.IClass
import org.testng.ITestNGMethod
import org.testng.ITestResult

class TestResultDTO implements ITestResult, Serializable {
    Throwable throwable
    int status
    ITestNGMethod method
    Object[] parameters = []
    IClass testClass
    long startMillis
    long endMillis
    String name

    TestResultDTO(ITestResult i){
        this.throwable = i.throwable
        this.status = i.status
        this.method = new TestNGMethodDTO(i.method)
        this.parameters = i.parameters
        this.testClass = new ClassDTO(i.testClass)
        this.startMillis = i.startMillis
        this.endMillis = i.endMillis
        this.name = i.name
    }

//unused
    boolean success

    @Override
    String getHost() {
        return null
    }

    @Override
    Object getInstance() {
        return null
    }

    @Override
    int compareTo(ITestResult o) {
        return 0
    }

    @Override
    Object getAttribute(String s) {
        return null
    }

    @Override
    void setAttribute(String s, Object o) {

    }
}
