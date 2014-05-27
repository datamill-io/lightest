package com.googlecode.lightest.core

import java.util.List;

/**
 * Basic implementation of a dispatcher assignment strategy that always fails
 * to assign a test case to a dispatcher.  This allows for easy testing of
 * failure cases.
 */
class NonAssigningDispatcherAssignmentStrategy
    implements IDispatcherAssignmentStrategy {
    void setDispatchers(List<ITaskDispatcher> dispatchers) {}
    boolean assign(LightestTestCase testcase) { return false }
    void unassign(LightestTestCase testcase) {}
}