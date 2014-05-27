package com.googlecode.lightest.report.dto

import org.testng.IResultMap
import org.testng.ISuite
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.xml.XmlTest

class TestContextDTO implements ITestContext, Serializable {
    private static final int serialVersionUID = 1L
    String name
    Date startDate
    Date endDate
    IResultMap passedTests = new ResultMapDTO()
    IResultMap skippedTests = new ResultMapDTO()
    IResultMap failedTests = new ResultMapDTO()
    IResultMap passedConfigurations = new ResultMapDTO()
    IResultMap skippedConfigurations = new ResultMapDTO()
    IResultMap failedConfigurations = new ResultMapDTO()
    ISuite suite

    TestContextDTO(ITestContext testContext, ISuite parentSuite){
        this(testContext.name, parentSuite)
    }

    TestContextDTO(String testName, ISuite parentSuite){
        this.name = testName
        this.suite = parentSuite
    }

    ITestNGMethod[] getAllTestMethods(){
        def allMethods = []
        allMethods.addAll(passedTests.allMethods)
        allMethods.addAll(skippedTests.allMethods)
        allMethods.addAll(failedTests.allMethods)
        allMethods.toArray()
    }

    //unused

    IResultMap failedButWithinSuccessPercentageTests
    String[] includedGroups
    String[] excludedGroups
    String outputDirectory
    String host
    Collection<ITestNGMethod> excludedMethods
    XmlTest currentXmlTest
    private HashMap<String, Serializable> attributes = new HashMap<>()
    @Override
    Object getAttribute(String s) {
        return attributes.get(s)
    }

    @Override
    void setAttribute(String s, Object o) {
        if (o instanceof Serializable){
            attributes.put(s,o)
        }

    }

    void addResult(ITestResult result) {
        if (!startDate){
            startDate = new Date(result.startMillis)
        }
        def method = result.getMethod()
        switch (result.status){
            case ITestResult.SUCCESS:
                passedTests.addResult(result, method)
                break
            case ITestResult.FAILURE:
                failedTests.addResult(result, method)
                break
            case ITestResult.SKIP:
                skippedTests.addResult(result, method)
                break
        }
    }
}
