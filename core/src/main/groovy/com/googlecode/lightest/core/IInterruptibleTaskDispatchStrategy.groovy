package com.googlecode.lightest.core

/**
 * A dispatch strategy that can be interrupted. This may be to hand control
 * over to an interactive user, or for other reasons. Even though this strategy
 * generally supports being interrupted, there may be situations where
 * interruption is unallowed, as indicated by the interruptible property.*/
interface IInterruptibleTaskDispatchStrategy extends ITaskDispatchStrategy {

    /**
     * Sets whether interrupt() is allowed to be called on this strategy.
     *
     * @param interruptible
     */
    void setInterruptible(boolean interruptible)

    /**
     * Returns whether interrupt() is allowed to be called on this strategy.*/
    boolean isInterruptible()

    /**
     * Notifies the dispatch strategy of the intent to interrupt. This method
     * should check the interruptible flag to see if interruption is allowed.*/
    void interrupt()
}