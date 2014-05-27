package com.googlecode.lightest.core

/**
 * The context available to tasks any test thread. This object is typically
 * managed by the TaskDispatcher, which is capable of associating the
 * environment and testcase with it.*/
class LightestContext {
    /** the directory in which the report will be generated */
    String outputDir

    protected IPreferences prefs
    protected ITestEnvironment env
    protected LightestTestCase testcase

    private Stack<ContextCarryingTaskProvider> taskProviders
    private Map<String, Object> vars

    LightestContext() {
        taskProviders = new Stack<ITaskProvider>()
        vars = [:]
    }

    /**
     * Sets the preferences, environment, and testcase from the specified
     * parent context.
     *
     * @param parentContext the context to inherit values from
     */
    void inheritContextFromParent(LightestContext parentContext) {
        prefs = parentContext.getPrefs()
        env = parentContext.getEnv()
        testcase = parentContext.testcase
    }

    /**
     * The test run preferences are accessible from the context, but should not
     * be settable or modifiable.*/
    IPreferences getPrefs() {
        return prefs?.clone()
    }

    /**
     * The test environment is accessible from the context, but it should not
     * be settable or modifiable.*/
    ITestEnvironment getEnv() {
        return env?.clone()
    }

    /**
     * Returns the current task provider. The testcase must be set on this
     * context before this method is called. If the current provider's builder
     * is in the midst of building a TaskNode tree, the builder itself is
     * returned as the provider.*/
    ITaskProvider getTaskProvider() {
        assert testcase != null

        def provider = taskProviders.empty() ? testcase : taskProviders.peek()
        def builder = provider.getBuilder()

        return builder.hasCurrentNode() ? builder : provider
    }

    protected void pushTaskProvider(ITaskProvider taskProvider) {
        taskProviders.push(new ContextCarryingTaskProvider(taskProvider))
    }

    protected ITaskProvider popTaskProvider() {
        return taskProviders.pop()
    }

    /**
     * Push a variable value onto the context. Normally when values are set on
     * the context, they overwrite previously set values. Use push() to retain
     * the current value; future property accesses will see the new value. The
     * pushed value will remain in effect as long as the current task provider
     * is on the stack; when it is removed, the value is also automatically
     * removed. In other words, this method provides the ability to set
     * task-scoped context values. Calling push() multiple times with the same
     * variable name will not grow a stack.
     *
     * @param name the name of the context variable
     * @param value the new value of the context variable
     */
    Object push(Object name, Object value) {
        taskProviders.peek().put(name, value)
    }

    /**
     * Removes all local context values from the context. This will not clear
     * context values that are have been added via push().*/
    void reset() {
        vars = [:]
    }

    /**
     * Local context values are magically accessible as properties of the
     * context object. See push() for differences.
     *
     * @param name
     * @param value
     */
    private propertyMissing(String name, value) {
        vars[name] = value
    }

    /**
     * Local context values are magically accessible as properties of the
     * context object.
     *
     * @param name
     */
    private propertyMissing(String name) {
        for (taskProvider in taskProviders.reverse()) {
            if (taskProvider.containsKey(name)) {
                return taskProvider.get(name)
            }
        }

        return vars[name]
    }
}
