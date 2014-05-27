package com.googlecode.lightest.core

class SimpleTaskDispatchStrategy implements ITaskDispatchStrategy {

    ITaskResult dispatch(ITask task) {
        return task.perform()
    }
}
