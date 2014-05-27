package com.googlecode.lightest.core

interface IDispatcherAssignmentStrategy {

    /**
     * Sets the ITaskDispatchers this strategy is capable of assigning
     * testcases to.
     *
     * @param dispatchers
     */
    void setDispatchers(List<ITaskDispatcher> dispatchers)

    /**
     * Assigns a dispatcher to a testcase, and the testcase to the dispatcher.
     * Returns true if the assignment was successful, and false otherwise.
     *
     * @param testcase
     */
    boolean assign(LightestTestCase testcase)

    /**
     * Breaks the bi-directional association between a dispatcher and a
     * testcase.
     *
     * @param testcase
     */
    void unassign(LightestTestCase testcase)
}
