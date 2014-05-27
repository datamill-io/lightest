package com.googlecode.lightest.core

import com.googlecode.lightest.core.circuitbreaker.CircuitBreaker

/**
 * A bare-bones subclass of QueuedDispatcherAssignmentStrategy that should be
 * sufficient for most cases of multi-environment test runs.*/
class SimpleDispatcherAssignmentStrategy
        extends QueuedDispatcherAssignmentStrategy
        implements IDispatcherAssignmentStrategy {
    private List<ITaskDispatcher> dispatchers
    private CircuitBreaker cb

    SimpleDispatcherAssignmentStrategy() {
        dispatchers = []
        cb = CircuitBreaker.getInstance()
    }

    void setDispatchers(List<ITaskDispatcher> dispatchers) {
        this.dispatchers = dispatchers
    }

    /**
     * The canRunIn() method on the testcase is used as the compatibility test.
     * If it returns true for the environment associated with a given
     * dispatcher, that dispatcher is considered compatible, and will be
     * included in the returned list. Otherwise, it will be excluded.
     *
     * @param testcase the testcase for which to return a list of compatible
     *                  dispatchers
     */
    protected List<ITaskDispatcher> getCompatibleDispatchers(LightestTestCase testcase) {
        def compatibleDispatchers = []

        //need CB check here to check CB in between test classes
        if (cb.isClosed()) {
            for (dispatcher in dispatchers) {
                if (testcase.canRunIn(dispatcher.getContext().env)) {
                    compatibleDispatchers << dispatcher
                }
            }
        }

        return compatibleDispatchers
    }
}
