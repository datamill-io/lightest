package com.googlecode.lightest.core

/**
 * Represents a task that can have a breakpoint set on it, in order to pause
 * its execution.*/
interface IBreakpointTask extends ITask {

    /**
     * Returns true if this task has its breakpoint set, and false otherwise.*/
    boolean isBreakpoint()
}