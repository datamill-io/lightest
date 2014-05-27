package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import org.testng.ISuiteListener
import org.testng.ITestListener
import org.testng.ITestResult
import org.testng.IReporter

interface ILightestTestListener extends ISuiteListener, ITestListener, IReporter {

    /**
     * Sets the registry of pending tests, which are tests that will be run that
     * have not yet been reported on. Tests will be removed from the registry
     * once they are reported as being run.
     *
     * @param registry
     */
    void setRegistry(TestRegistry registry)

    /**
     * Returns the registry of pending tests.*/
    TestRegistry getRegistry()

    /**
     * Records the mapping between a test class and the environment in which
     * the test was performed.
     *
     * @param testClass
     * @param env
     */
    void addEnvironmentMapping(Class testClass, ITestEnvironment env)

    /**
     * This listener is the mechanism through which the dispatcher assignment
     * strategy is communicated to each testcase. The strategy is set on the
     * context object when onTestStart() is called. Thus, the strategy must be
     * set on this listener prior to any tests being run.
     *
     * @param strategy
     */
    void setDispatcherAssignmentStrategy(IDispatcherAssignmentStrategy strategy)

    /**
     * Invoked when a test finishes, whether successfully or not (or skipped).
     *
     * @param result
     */
    void onTestFinish(ITestResult result)

    /**
     * Invoked each time a task finishes performing.
     *
     * @param taskResult the results of performing the task
     * @param testResult the ITestResult associated with the currently
     *                    executing test
     */
    void onTaskComplete(ITaskResult taskResult, ITestResult testResult)

    /**
     * Registers a new reporter to be notified of reportable events. The
     * reporter should implement at least one of org.testng.IReporter or
     * ILightestReporter.
     *
     * @param reporter
     */
    void registerReporter(Object reporter)

    /**
     * Invokes generateBaseReport() on all registered reporters that are
     * instances of ILightestReporter.
     *
     * @param configText the textual configuration that was used to configure
     *                    this run, if any. May be null.
     */
    void initializeReporters(String configText)

    /**
     * Sets the output directory to be propagated to all registered
     * reporters.*/
    void setOutputDir(String outputDir)
}
