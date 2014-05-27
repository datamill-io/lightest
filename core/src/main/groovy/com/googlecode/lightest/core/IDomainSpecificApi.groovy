package com.googlecode.lightest.core

interface IDomainSpecificApi {

    /**
     * Returns a task, if the task is defined in this API. Returns null
     * otherwise.
     *
     * @param name
     */
    ITask getTask(String name)
}
