package com.googlecode.lightest.core

import java.lang.reflect.Method

import org.testng.annotations.*
import org.testng.ITestContext
import org.testng.Reporter
import org.testng.SkipException
import org.testng.SuiteResult

/**
 * The base class for all testcase classes runnable in the Lightest framework.
 * In order to enable any domain specific API's in this testcase, an
 * implementation of IDomainSpecificApi should be set with setApi(). One
 * technique to do this is to subclass this class, set the API in a @BeforeTest
 * method (or constructor), and derive test classes from that subclass.*/
class LightestTestCase extends LightestContextAware implements ITaskProvider {
    private static final Set<String> CONFIG_METHOD_NAMES = new HashSet<String>(
            ['assign', 'setUp', 'tearDown', 'unassign']).asImmutable()

    IDomainSpecificApi api

    private ThreadLocal<TaskNodeBuilder> builder

    LightestTestCase() {
        builder = new ThreadLocal<TaskNodeBuilder>()
    }

    /**
     * Assigns this test class instance to a task dispatcher.*/
    @BeforeClass(alwaysRun = true)
    final void assign(ITestContext context) {
        // the SkipException previously thrown here is now produced by
        // LightestTestListener.beforeInvocation()
        boolean assigned = context
                .getAttribute(LightestTestListener.ATTR_STRATEGY).assign(this)
    }

    /**
     * Performs per-method setup.*/
    @BeforeMethod(alwaysRun = true)
    final void setUp() {
        builder.set(new TaskNodeBuilder())
    }

    /**
     * Here we tamper with the TestNG suite internals so the suite results is
     * available for intermediate report generation. We are bad!!!*/
    @AfterMethod(alwaysRun = true)
    final void tearDown(ITestContext testngContext) {
        synchronized (testngContext) {
            def suite = testngContext.getSuite()
            def suiteResult = new SuiteResult(suite.getXmlSuite(),
                                              testngContext)

            // the suite runner will safely overwrite this when it wants to
            suite.getResults().put(testngContext.getName(), suiteResult)
        }

        builder.remove()
    }

    /**
     * Unbinds the assigned task dispatcher from this test class.*/
    @AfterClass(alwaysRun = true)
    final void unassign(ITestContext context) {
        context.getAttribute(LightestTestListener.ATTR_STRATEGY).unassign(this)
    }

    TaskNodeBuilder getBuilder() {
        return builder.get()
    }

    /**
     * Used to indicate whether the current test class can run in the given
     * environment; i.e.&nbsp;whether the environment satisfies the basic
     * requirements for the test. The default implementation simply returns
     * true; subclasses should override this method to change its behavior.
     * Although this method hints at which dispatchers are appropriate for this
     * test class, different implementations of IDispatcherAssignmentStrategy
     * may ignore it.
     *
     * @param env the environment for which to tell whether this test can run
     */
    boolean canRunIn(ITestEnvironment env) {
        return true
    }

    /**
     * Returns the names of the configuration methods considered part of the
     * framework (as opposed to being test-specific) when tests are extended
     * from this class. Overridding methods should include a call to super.*/
    Set<String> getFrameworkMethodNames() {
        // defensive copy
        return new HashSet<String>(CONFIG_METHOD_NAMES)
    }

    /**
     * Attempts to treat calls to missing methods as API task invocations.
     *
     * Task invocations may be specified with a builder-like nested syntax. If
     * specified in this way, they will be invoked in depth-first order, with
     * the results of the parent tasks available to be referenced by the child
     * tasks via the task results hierarchy. The configuration of parent tasks
     * is also available to child tasks.
     *
     * If a parent task fails, the child tasks will not be performed. However,
     * sibling tasks will be performed.*/
    private ITaskResult methodMissing(String name, args) {
        LightestUtils.setBuilderAsDelegate(args, builder.get())

        def taskTree = builder.get().invokeMethod(name, args)

        return dispatcher.dispatch(taskTree, null,
                                   Reporter.getCurrentTestResult())
    }
}
