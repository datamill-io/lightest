package com.googlecode.lightest.core

/**
 * The default implementation of ITaskResult.*/
class TaskResult implements ITaskResult {
    ITaskResult parent
    String message
    String detailedMessage
    long startTime
    long endTime

    private transient ITask task
    private List<ITaskResult> childResults
    private List<TaskResultLink> links
    private int status

    /**
     * Constructs a default task results. The default status is always
     * ITaskResult.STATUS_OK .*/
    TaskResult(ITask task) {
        this(task, null)
    }

    TaskResult(ITask task, ITaskResult parent) {
        this.task = task
        this.parent = parent
        childResults = []
        message = ""
        detailedMessage = ""
        links = []
        status = STATUS_OK

        if (parent != null) {
            parent.appendChild(this)
        }
    }

    /**
     * Returns the list of URL's associated with this results.*/
    List<TaskResultLink> getLinks() {
        return links
    }

    /**
     * Returns the status of the results.*/
    int getStatus() {
        return status
    }

    /**
     * Returns the task whose performance returned this results.*/
    ITask getTask() {
        return task
    }

    /**
     * Adds a URL referencing a resource that provides additional information
     * related to this results.
     *
     * @param link the string representation of the URL to add
     */
    void addLink(String link) {
        links << new TaskResultLink(href: link, text: link)
    }

    void addLink(TaskResultLink link) {
        links << link
    }

    /**
     * Adds to the list of child task results.*/
    void appendChild(ITaskResult childResult) {
        childResults << childResult
    }

    /**
     * Returns a list of child task results.*/
    List<ITaskResult> children() {
        return childResults
    }

    /**
     * Sets the status of this results to STATUS_FLAGGED, but only if it is not
     * already STATUS_FAILED. The default status of the task should be
     * STATUS_OK.*/
    void flag() {
        setStatus(STATUS_FLAGGED)
    }

    /**
     * Sets the status of this results to STATUS_FAILED, but only if it is not
     * already STATUS_DOOMED. The default status of the task should be
     * STATUS_OK.*/
    void fail() {
        setStatus(STATUS_FAILED)
    }

    /**
     * Sets the status of this results to STATUS_DOOMED, which indicates that
     * the failure of the task means the remainder of the test cannot
     * continue successfully, and should be aborted.*/
    void doom() {
        setStatus(STATUS_DOOMED)
    }

    protected void setStatus(newStatus) {
        if (status < newStatus) {
            status = newStatus
        }
        parent?.setStatus(newStatus)
    }
}
