package com.googlecode.lightest.core

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Assigns testcases to dispatchers in a FIFO manner. Each testcase passed into
 * assign() waits in a queue until getAvailableDispatchers() returns an idle
 * ITaskDispatcher object the testcase can be assigned to.*/
abstract class QueuedDispatcherAssignmentStrategy
        implements IDispatcherAssignmentStrategy {
    private List<LatchedTestCase> assignmentQueue
    private long assignmentTimeout
    def config = new Configuration()

    QueuedDispatcherAssignmentStrategy() {
        assignmentQueue = [].asSynchronized()
        assignmentTimeout = 0
    }

    /**
     * Sets the maximum time a testcase will wait to have a task dispatcher
     * assigned to it, before timing out.
     *
     * @param timeout allotted time, in milliseconds. Non-positive values will
     *                 be considered infinite time.
     */
    void setAssignmentTimeout(long timeout) {
        assignmentTimeout = timeout
    }

    /**
     * Assigns a dispatcher to a testcase, and the testcase to the dispatcher.
     * Returns true if the assignment was successful, and false otherwise.
     * Testcases that cannot be immediately assigned are queued up for
     * assignment in FIFO order, and stay in the queue until assigned, or the
     * timeout elapses (if any).
     *
     * @param testcase
     *
     * @throws IllegalArgumentException if none of the dispatchers are suitable for the testcase
     */
    boolean assign(LightestTestCase testcase) {
        // If none of our environment support this test case, fail now rather
        // than waiting for the full timeout.
        // Follow this yellow brick (method) road fo CB check
        if (getCompatibleDispatchers(testcase).isEmpty()) {
            return false
        }

        def done = new CountDownLatch(1)

        assignmentQueue << new LatchedTestCase(testcase, done)
        updateAssignments()

        if (assignmentTimeout > 0) {
            return done.await(assignmentTimeout, TimeUnit.MILLISECONDS)
        }

        done.await()
        return true
    }

    void unassign(LightestTestCase testcase) {
        def dispatcher = testcase.getDispatcher()

        dispatcher?.setTestCase(null)
        testcase.setDispatcher(null)

        updateAssignments()
    }

    /**
     * Assigns each testcase to the first available dispatcher that can service
     * it, as defined by getAvailableDispatchers().*/
    private void updateAssignments() {
        synchronized (this) {
            def i = 0  // avoid iterator, ConcurrentModificationException

            while (i < assignmentQueue.size()) {
                def entry = assignmentQueue[i]
                def testcase = entry.getTestCase()
                def dispatchers = getAvailableDispatchers(testcase)

                if (dispatchers.size() > 0) {
                    def dispatcher = dispatchers[0]

                    dispatcher.setTestCase(testcase)
                    testcase.setDispatcher(dispatcher)

                    entry.getLatch().countDown()
                    assignmentQueue.remove(i)
                } else {
                    ++i
                }
            }
        }
    }

    /**
     * Returns a potentially empty list of currently available (unassigned)
     * task dispatchers for a given testcase. This accommodates the possibility
     * of filtering dispatchers based on testcase attributes, by starting with
     * the results of getCompatibleDispatchers() passing in the testcase.
     *
     * @param testcase the testcase for which to return the dispatchers
     */
    protected List<ITaskDispatcher> getAvailableDispatchers(LightestTestCase testcase) {
        def dispatchers = []

        getCompatibleDispatchers(testcase).each {
            if (it.getTestCase() == null) {
                dispatchers << it
            }
        }

        return dispatchers
    }

    protected abstract List<ITaskDispatcher> getCompatibleDispatchers(LightestTestCase testcase)
}