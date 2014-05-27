package com.googlecode.lightest.core

import org.testng.ITestResult

/**
 * Responsible for actually performing tasks.*/
class TaskDispatcher implements ITaskDispatcher {
    private ITaskDispatchStrategy strategy
    private ILightestTestListener listener
    private ThreadedLightestContext context

    TaskDispatcher(ThreadedLightestContext context) {
        this.context = context
    }

    void setPreferences(IPreferences prefs) {
        context.get().prefs = prefs
    }

    void setEnvironment(ITestEnvironment env) {
        context.get().env = env
    }

    /**
     * @param strategy
     */
    void setStrategy(ITaskDispatchStrategy strategy) {
        this.strategy = strategy
    }

    void setListener(ILightestTestListener listener) {
        this.listener = listener
    }

    /**
     * Assigns a testcase to this dispatcher. The testcase can be unassigned
     * by passing in null.
     *
     * Makes the association between the test class and its test environment,
     * which eventually will be needed by the report, available to the
     * listener. We perform the mapping through the listener because TestNG
     * does not allow us to inject this information any other way.
     *
     * @param testcase
     */
    void setTestCase(LightestTestCase testcase) {
        if (testcase != null) {
            assert listener != null
            listener.addEnvironmentMapping(testcase.class, context.get().env)
        }

        context.get().testcase = testcase
    }

    /**
     * Returns the testcase this dispatcher is currently assigned to, or null
     * if it is not assigned to a testcase.*/
    LightestTestCase getTestCase() {
        return context.get().testcase
    }

    /**
     * Returns the thread local context associated with this dispatcher, for
     * the currently executing thread.*/
    LightestContext getContext() {
        return context.get()
    }

    /**
     * Dispatches the task and all nested tasks represented by the Node tree,
     * as available in the API of the current testcase, and using the current
     * dispatch strategy. The currently attached listener will be notified of
     * the task results. "parentResult" will be null for root tasks. Returns the
     * results of dispatching the task.
     *
     * @param taskNode
     * @param parentResult
     */
    ITaskResult dispatch(TaskNode taskNode, ITaskResult parentResult,
                         ITestResult testResult) {
        def taskName = taskNode.name()
        def task = getTestCase().getApi().getTask(taskName)
        def taskResult

        if (task == null) {
            def args = [taskNode.attributes(), taskNode.value()]
            throw new MissingMethodException(taskName, this.class, args)
        }

        // set the dispatcher first - context is thus available in configure()
        task.setDispatcher(this)
        task.configure(taskNode, parentResult)

        context.get().pushTaskProvider(task)

        try {
            taskResult = strategy.dispatch(task)

            if (taskResult.getStatus() == ITaskResult.STATUS_DOOMED) {
                throw new TaskDoomedException(taskResult.getMessage())
            }

            listener.onTaskComplete(taskResult, testResult)

            if (taskResult.getStatus() != ITaskResult.STATUS_FAILED) {
                for (childTaskNode in taskNode.children()) {
                    dispatch(childTaskNode, taskResult, testResult)
                }
            }
        } finally {
            context.get().popTaskProvider()
        }

        return taskResult
    }
}
