package com.googlecode.lightest.core

/**
 * Represents a domain-specific task to be performed in a test. ITask's are
 * typically owned by IDomainSpecificApi's.*/
interface ITask extends ITaskProvider {

    /**
     * Configures the task
     *
     * @param config the configuration for this task. The Node tree may
     *                      also contain configuration information for any
     *                      parent and child tasks. If it does, this task
     *                      object may inspect, but should not mutate, any
     *                      Nodes it does not directly relate to.
     * @param parentResult the results of performing the parent task. May be
     *                      null if this task has no parent.
     */
    void configure(TaskNode config, ITaskResult parentResult)

    /**
     * Sets the dispatcher to be used to perform the task.
     *
     * @param dispatcher
     */
    void setDispatcher(ITaskDispatcher dispatcher)

    /**
     * Returns the name of the task.*/
    String getName()

    /**
     * Returns the abbreviated name of the task.*/
    String getShortName()

    /**
     * Returns a user supplied description of the task instance, its purpose,
     * etc.*/
    String getDescription()

    /**
     * Returns a new Map of names to values of the paremeters to the task. This
     * identifies how the task can be reproduced. Names are String's, values
     * are Object's.*/
    Map<String, Object> getParams()

    /**
     * Returns the value set as the node value in the configuration. This, like
     * getParams(), is information about how the task was configured.*/
    Object getValue()

    /**
     * Performs the task in an environment, and returns the results. Tasks may
     * choose to use or ignore the results of their parent tasks.*/
    ITaskResult perform()
}
