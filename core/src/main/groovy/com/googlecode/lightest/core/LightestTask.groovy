package com.googlecode.lightest.core

import org.testng.Reporter

/**
 * A convenient base class for implementations of ITask.*/
abstract class LightestTask extends LightestContextAware
        implements IBreakpointTask {
    protected TaskNode config
    protected ITaskResult parentResult
    protected ITaskResult taskResult

    private TaskNodeBuilder builder

    LightestTask() {
        builder = new TaskNodeBuilder()
    }

    /**
     * Returns a Set of this class' bean property name String's that can be
     * automatically set based on the TaskNode passed to the configure()
     * method. This Set is basically the full listing of settable properties
     * for this class, minus all properties of the LightestTask class. This
     * method can be overriden to exclude additional bean properties that
     * should not be auto-configured.*/
    protected Set<String> configurableProperties() {
        // this method's name does not start with "get"; this is intentional
        // to avoid recursion when inspecting the instance's properties
        def props = new HashSet<String>(this.properties.keySet())

        LightestTask.metaClass.properties.each {
            props.remove(it.name)
        }

        props.retainAll(LightestUtils.getSettableProperties(this.class))

        return props
    }

    // TODO - evaluate whether parentResult needs to be propagated, given that
    //        we now have the context object
    /**
     * Configures the task by making member variables available to concrete
     * subclasses, and automatically setting configurable bean properties.
     * Implicit conversion between Integer, String, and Boolean types is
     * supported. Override this method for different configuration behavior.
     *
     * @param config
     * @param parentResult the results of performing the parent task. May be
     *                      null if this task has no parent.
     */
    void configure(TaskNode config, ITaskResult parentResult) {
        this.config = config
        this.parentResult = parentResult

        def props = configurableProperties()

        config.attributes().each { name, value ->
            if (props.contains(name)) {
                def metaProp = this.class.metaClass.getMetaProperty(name)

                switch (metaProp.type.name) {
                    case ['int', 'java.lang.Integer']:
                        // value could be 0, which evaluates to false
                        if (value != null && value != "") {
                            this."${name}" = value.toInteger()
                        }
                        break

                    case ['boolean', 'java.lang.Boolean']:
                        this."${name}" = (value && value != 'false')
                        break

                    case 'java.lang.String':
                        this."${name}" = value?.toString()
                        break

                    default:
                        this."${name}" = value
                }
            }
        }

        taskResult = new TaskResult(this, parentResult)
    }

    /**
     * Returns the name of this task, which is the binary name of the class.*/
    String getName() {
        return this.class.name
    }

    /**
     * Returns the short name of the task, which is the unqualified class name.*/
    String getShortName() {
        return name.substring(name.lastIndexOf('.') + 1)
    }

    String getDescription() {
        assert config != null

        return config.'@description' ?: ""
    }

    Map<String, Object> getParams() {
        assert config != null

        def params = new TreeMap()
        def attributes = config.attributes().findAll { k, v -> k != 'description' && k != 'breakpoint'
        }

        params.putAll(attributes)

        return params
    }

    Object getValue() {
        assert config != null

        return config.nodeValue()
    }

    boolean isBreakpoint() {
        assert config != null

        return config.'@breakpoint'
    }

    /**
     * Concrete subclasses must implement this method, which is wrapped by
     * the perform() method. The results is passed in, so implementing methods
     * may simply populate it without needing to create it or return it.
     *
     * @param result the results which will be returned by the outer call to
     *                perform().
     */
    abstract void doPerform(ITaskResult result)

    /**
     * Performs the task in an environment, and returns the results. Tasks may
     * choose to use or ignore the results of their parent tasks. This
     * implementation wraps doPerform() so it can safely execute the task and
     * add some behaviors to the results.*/
    ITaskResult perform() {
        assert taskResult != null

        taskResult.setStartTime(System.currentTimeMillis())

        //Get test script execution point line number, right now adding via link
        def link = new TaskResultLink(href: "",
                                      rel: "",
                                      text: "Test script line number: " + LightestUtils.getTestCaseStackElementLineNumber())
        taskResult.addLink(link)

        try {
            doPerform(taskResult)
        } catch (e) {
            def stackTrace = LightestUtils.getSanitizedStackTrace(e)

            taskResult.setMessage("Unexpected exception: ${e.getMessage()}")
            taskResult.setDetailedMessage(stackTrace)
            taskResult.fail()
        }

        taskResult.setEndTime(System.currentTimeMillis())

        return taskResult
    }

    TaskNodeBuilder getBuilder() {
        return builder
    }

    /**
     * Allows for tasks to be created via composition of existing tasks. Tasks
     * created through composition are performed in the following manner:
     *
     *  1) The task is performed in full before any of its child tasks, as
     *     specified in the testcase, are performed.
     *  2) The "sub-tasks" comprising the task (not to be confused with child)
     *     tasks) are performed as if they were child tasks, and will be
     *     identified as such in the results (i.e. the listener attached to the)
     *     task dispatcher will be notified in this order).
     *  3) Bona-fide child tasks will only be performed in the event that none
     *     of the above tasks have failed. This is consistent with the behavior
     *     that is expected when child tasks are nested under non-composition
     *     tasks. They will appear as siblings of the "sub-task" branch.
     *
     * To illustrate:
     *
     *   - A, B, C, D, & E are tasks.
     *   - A.doPerform() has the following structure:
     *         doPerform() {*             B () {*                 C()
     *}*}*   - The testcase has the structure:
     * @Test
     * void testIt() {*             A () {*                D ()
     *                E ()
     *}*}*
     * Then the task results tree will look like:
     *
     *     A
     *     |
     *     +--B
     *     |  |
     *     |  +--C
     *     |
     *     +--D
     *     |
     *     +--E
     *
     * And this is the exact flow of execution, depth-first. In contrast to
     * specifying exactly this structure in the testcase, however, if either B
     * or C were to fail, D & E would NOT be performed. This is because the
     * latter are being treated as dependencies of the entire composite task A.
     */
    private ITaskResult methodMissing(String name, args) {
        assert dispatcher != null

        LightestUtils.setBuilderAsDelegate(args, builder)

        def taskTree = builder.invokeMethod(name, args)
        def result = dispatcher.dispatch(taskTree, this.taskResult,
                                         Reporter.getCurrentTestResult())

        if (result.getStatus() == ITaskResult.STATUS_FAILED) {
            // child tasks will NOT be performed
            while (config.children().size() > 0) {
                config.remove(config.children()[0])
            }

            // fail the current task. Note however that sibling sub-tasks will
            // continue to be executed.
            taskResult.fail()
        }

        return result
    }
}