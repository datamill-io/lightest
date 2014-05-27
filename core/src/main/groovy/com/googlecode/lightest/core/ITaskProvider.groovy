package com.googlecode.lightest.core

/**
 * A marker interface for classes that have a TaskNodeBuilder which can be used
 * to create TaskNode's representing task configurations. Implementing classes
 * may optionally implement a methodMissing() that delegates to the builder,
 * generates ITask's based on the task configurations, and sends them to an
 * ITaskDispatcher for dispatch. If implemented, this method should also return
 * an ITaskResult object. This method may be private.*/
interface ITaskProvider {

    /**
     * Returns the builder this task provider uses to create task nodes to be
     * sent for dispatch.*/
    TaskNodeBuilder getBuilder()
}
