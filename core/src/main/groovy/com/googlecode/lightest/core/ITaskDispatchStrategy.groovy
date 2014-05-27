package com.googlecode.lightest.core

/**
 * This strategy is used by TaskDispatcher to perform tasks. This extra layer
 * of indirection allows for additional logic to be injected before or after
 * each task is performed.*/
interface ITaskDispatchStrategy {

    /**
     * At the very least, calls the perform() method on the task.
     *
     * @param task
     */
    ITaskResult dispatch(ITask task)
}
