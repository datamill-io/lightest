package com.googlecode.lightest.report.dto

import org.testng.IAttributes
import org.testng.IObjectFactory
import org.testng.ISuite
import org.testng.ISuiteResult
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.SuiteRunState
import org.testng.internal.annotations.IAnnotationFinder
import org.testng.xml.XmlSuite

class SuiteDTO implements Serializable, ISuite {
    private static final int serialVersionUID = 1L
    String name
    Map<String, ISuiteResult> results = [:]
    Map<String, Collection<ITestNGMethod>> methodsByGroups = [:]
    Map<String, Object> attributes = [:]
    XmlSuite xmlSuite
    String outputDirectory

    @Override
    Object getAttribute(String s) {
        return attributes.get(s)
    }

    @Override
    void setAttribute(String s, Object o) {
        attributes.put(s,o)
    }

    Collection<ITestNGMethod> invokedMethods = [] as Set

    SuiteDTO(ISuite is, String name){
        this.name = name
        this.methodsByGroups = is.getMethodsByGroups()
        for (def entry: is.results){
            results.put(entry.key, new SuiteResultDTO(entry.value, this))
        }
    }

    // unused

    @Override
    IObjectFactory getObjectFactory() {
        return null
    }

    @Override
    String getParallel() {
        return null
    }

    @Override
    String getParameter(String s) {
        return null
    }



    @Override
    Collection<ITestNGMethod> getExcludedMethods() {
        return null
    }

    @Override
    void run() {}

    @Override
    String getHost() {
        return null
    }

    @Override
    SuiteRunState getSuiteState() {
        return null
    }

    @Override
    IAnnotationFinder getAnnotationFinder(String s) {
        return null
    }

    void addResult(ITestResult result, String testName) {
        ISuiteResult suiteResult = getSuiteResultOrMakeNew(testName)

        suiteResult.testContext.addResult(result)
        invokedMethods << result.method
    }

    private ISuiteResult getSuiteResultOrMakeNew(String testName) {
        def suiteResult = results.get(testName)
        if (!suiteResult) {
            suiteResult = new SuiteResultDTO(testName, this)
            results.put(testName, suiteResult)
        }
        suiteResult
    }

    void completed(String testName, Date endDate) {
        ISuiteResult suiteResult = getSuiteResultOrMakeNew(testName)
        suiteResult.testContext.endDate = endDate
    }
}
