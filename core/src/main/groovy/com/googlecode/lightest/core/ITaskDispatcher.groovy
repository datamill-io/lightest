package com.googlecode.lightest.core

import org.testng.ITestResult

/**
 * Responsible for actually performing tasks. Each dispatcher has a 1-1
 * relationship with an ITestEnvironment.*/
interface ITaskDispatcher {

    void setPreferences(IPreferences prefs)

    void setEnvironment(ITestEnvironment env)

    /**
     * @param strategy
     */
    void setStrategy(ITaskDispatchStrategy strategy)

    void setListener(ILightestTestListener listener)

    /**
     * Assigns a testcase to this dispatcher.
     *
     * @param testcase
     */
    void setTestCase(LightestTestCase testcase)

    /**
     * Returns the testcase this dispatcher is currently assigned to, or null
     * if it is not assigned to a testcase.*/
    LightestTestCase getTestCase()

    /**
     * Returns the context object associated with this dispatcher. Environment
     * and preference information are available via this object.*/
    LightestContext getContext()

    /**
     * Dispatches the task and all nested tasks represented by the Node tree,
     * as available in the API of the current testcase, and using the current
     * dispatch strategy. The currently attached listener will be notified of
     * the task results. "parentResult" will be null for root tasks. Returns the
     * results of dispatching the task.
     *
     * @param taskNode
     * @param parentResult
     * @param testResult
     */
    ITaskResult dispatch(TaskNode taskNode, ITaskResult parentResult,
                         ITestResult testResult)
}