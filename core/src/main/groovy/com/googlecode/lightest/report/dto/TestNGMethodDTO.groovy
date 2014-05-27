package com.googlecode.lightest.report.dto

import org.testng.IClass
import org.testng.IRetryAnalyzer
import org.testng.ITest
import org.testng.ITestClass
import org.testng.ITestNGMethod

import java.lang.reflect.Method

class TestNGMethodDTO implements ITestNGMethod, Serializable{
    String[] groupsDependedUpon
    String[] methodsDependedUpon
    String[] groups
    String description
    boolean test
    String methodName
    Class realClass
    String signature

    TestNGMethodDTO(ITestNGMethod method){
        this.groupsDependedUpon = method.groupsDependedUpon
        this.methodsDependedUpon = method.methodsDependedUpon
        this.groups = method.groups
        this.description = method.description
        this.test = method.test
        this.methodName = method.methodName
        this.realClass = method.realClass
        this.signature = method.signature
    }

    //unused

    @Override
    ITestClass getTestClass() {
        return null
    }

    @Override
    void setTestClass(ITestClass iTestClass) {

    }

    @Override
    Method getMethod() {
        return null
    }

    @Override
    Object[] getInstances() {
        return new Object[0]
    }

    @Override
    long[] getInstanceHashCodes() {
        return new long[0]
    }

    @Override
    String getMissingGroup() {
        return null
    }

    @Override
    void setMissingGroup(String s) {

    }

    @Override
    String[] getBeforeGroups() {
        return new String[0]
    }

    @Override
    String[] getAfterGroups() {
        return new String[0]
    }

    @Override
    void addMethodDependedUpon(String s) {

    }


    @Override
    boolean isBeforeMethodConfiguration() {
        return false
    }

    @Override
    boolean isAfterMethodConfiguration() {
        return false
    }

    @Override
    boolean isBeforeClassConfiguration() {
        return false
    }

    @Override
    boolean isAfterClassConfiguration() {
        return false
    }

    @Override
    boolean isBeforeSuiteConfiguration() {
        return false
    }

    @Override
    boolean isAfterSuiteConfiguration() {
        return false
    }

    @Override
    boolean isBeforeTestConfiguration() {
        return false
    }

    @Override
    boolean isAfterTestConfiguration() {
        return false
    }

    @Override
    boolean isBeforeGroupsConfiguration() {
        return false
    }

    @Override
    boolean isAfterGroupsConfiguration() {
        return false
    }

    @Override
    long getTimeOut() {
        return 0
    }

    @Override
    int getInvocationCount() {
        return 0
    }

    @Override
    void setInvocationCount(int i) {

    }

    @Override
    int getSuccessPercentage() {
        return 0
    }

    @Override
    String getId() {
        return null
    }

    @Override
    void setId(String s) {

    }

    @Override
    long getDate() {
        return 0
    }

    @Override
    void setDate(long l) {

    }

    @Override
    boolean canRunFromClass(IClass iClass) {
        return false
    }

    @Override
    boolean isAlwaysRun() {
        return false
    }

    @Override
    int getThreadPoolSize() {
        return 0
    }

    @Override
    void setThreadPoolSize(int i) {

    }

    @Override
    void incrementCurrentInvocationCount() {

    }

    @Override
    int getCurrentInvocationCount() {
        return 0
    }

    @Override
    void setParameterInvocationCount(int i) {

    }

    @Override
    int getParameterInvocationCount() {
        return 0
    }

    @Override
    ITestNGMethod clone() {
        return null
    }

    @Override
    IRetryAnalyzer getRetryAnalyzer() {
        return null
    }

    @Override
    void setRetryAnalyzer(IRetryAnalyzer iRetryAnalyzer) {

    }

    @Override
    boolean skipFailedInvocations() {
        return false
    }

    @Override
    void setSkipFailedInvocations(boolean b) {

    }

    @Override
    long getInvocationTimeOut() {
        return 0
    }

    @Override
    boolean ignoreMissingDependencies() {
        return false
    }

    @Override
    void setIgnoreMissingDependencies(boolean b) {

    }

    @Override
    int compareTo(Object o) {
        return 0
    }

    @Override
    public String toString() {
        return signature
    }
}
