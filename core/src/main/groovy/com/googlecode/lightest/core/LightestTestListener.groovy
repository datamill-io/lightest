package com.googlecode.lightest.core

import com.googlecode.lightest.core.circuitbreaker.CircuitBreaker
import com.googlecode.lightest.core.TestRegistry

import java.util.concurrent.atomic.AtomicInteger
import org.codehaus.groovy.runtime.StackTraceUtils
import org.testng.internal.ResultMap
import org.testng.IInvokedMethod
import org.testng.IInvokedMethodListener
import org.testng.IResultMap
import org.testng.ISuite
import org.testng.ITestClass
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.SkipException
import org.testng.TestListenerAdapter
import org.testng.xml.XmlSuite

/**
 * This class is responsible for wiring task dispatchers to testcases, and
 * recording the results of running tasks. It also notifies any registered
 * reporters to generate reports at appropriate points in the test execution
 * lifecycle.
 *
 * It supports two styles of reporters: ones that implement IReporter, and ones
 * that implement ILightestReporter. The former is used to create a single
 * report at the end of the suite run, while the latter may be updated
 * throughout the run, as tests are completed.*/
class LightestTestListener extends TestListenerAdapter
        implements ILightestTestListener, IInvokedMethodListener {
    public static final String ATTR_STRATEGY = 'strategy'
    TestRegistry registry
    String outputDir

    /** the default implementation is an in-memory Map. */
    Map<ITestResult, List<ITaskResult>> taskResultMap

    private InheritableThreadLocal<TestNGContext> testngContext
    private Map<Class, ITestEnvironment> envMap
    private IDispatcherAssignmentStrategy strategy
    private ReporterInvoker reporterInvoker
    private AtomicInteger resultCounter
    private CircuitBreaker cb

    LightestTestListener() {
        testngContext = new InheritableThreadLocal<TestNGContext>()
        envMap = Collections.synchronizedMap([:])
        reporterInvoker = new ReporterInvoker()
        registry = new TestRegistry()
        taskResultMap = Collections.synchronizedMap([:])
        resultCounter = new AtomicInteger(1)
        cb = CircuitBreaker.getInstance()
    }

    /**
     * Records the mapping between a test class and the environment in which
     * the test was performed.
     *
     * @param testClass
     * @param env
     */
    void addEnvironmentMapping(Class testClass, ITestEnvironment env) {
        if (envMap[testClass] == null) {
            envMap[testClass] = env
        }
    }

    void setDispatcherAssignmentStrategy(IDispatcherAssignmentStrategy strategy) {
        this.strategy = strategy
    }

    /**
     * Implemented to satisfy the ISuiteListener interface. A no-op.
     *
     * @param suite
     */
    void onFinish(ISuite suite) {}

    /**
     * Generates the base report for all registered ILightestReporter's .
     *
     * @param suite
     */
    void onStart(ISuite suite) {
        reporterInvoker.updateReport(suite, registry, outputDir)
    }

    /**
     * Ensures the list of failed tests maintained by the TestNG TestRunner
     * implementation underlying the ITestContext reflects the actual test
     * results status. We do this in the listener because the list is
     * manipulated by the TestNG Invoker, which we can't get our hands around.
     *
     * Yes, this is hackish - we're seriously meddling with the TestRunner
     * internals - but we've got to do it!
     *
     * @param context
     */
    @Override
    void onFinish(ITestContext context) {
        // TODO - add synchronization here?
        def testRunner = (org.testng.TestRunner) context
        IResultMap passedTests = new ResultMap()

        for (testResult in testRunner.getPassedTests().getAllResults()) {
            if (testResult.getStatus() == ITestResult.FAILURE) {
                testRunner.addFailedTest(testResult.getMethod(), testResult)
            } else {
                passedTests.addResult(testResult, testResult.getMethod())
            }
        }

        // did we recategorize passed tests as failures just now? if so ...
        if (passedTests.size() != testRunner.getPassedTests().size()) {
            // ... update the TestRunner internal variable! Yikes!
            testRunner.m_passedTests = passedTests
        }

        super.onFinish(context)
        testngContext.set(null)
    }

    @Override
    void onStart(ITestContext context) {
        super.onStart(context)
        context.setAttribute(ATTR_STRATEGY, strategy)
        testngContext.set(new TestNGContext(context))
    }

    @Override
    void onTestFailure(ITestResult result) {
        //def currentTestId = resultCounter.get()

        resolveResult(result)
        super.onTestFailure(result)

        if (result.getThrowable() != null) {
            String message = StackTraceUtils.deepSanitize(result.getThrowable())
            StackTraceUtils.deepSanitize(result.getThrowable())

            if (message.contains("timed out waiting for ConditionWatch()")) {
                cb.addTimeout(envMap[result.getTestClass().getRealClass()].id)
                if (!cb.isClosed()) {
                    result.setThrowable(new Throwable(
                            "\n\nFATAL ERROR: CIRCUIT BREAKER OPEN\nMAX THRESHOLD OF " + cb.getThreshold() + " TIMEOUTS ON TWO ENVIRONMENTS EXCEEDED.\n" + "OR " + cb.getCeiling() + " TIMEOUTS ON ONE MACHINE\n" + cb.getCBData()))
                    String cb_tripped_file = ".\\lightest-report\\cb_tripped.txt"
                    File f = new File(cb_tripped_file)
                    f.createNewFile()
                }
            } else {
                cb.maybeResetTimeout(envMap[result.getTestClass().getRealClass()].id)
            }
        } else {
            cb.maybeResetTimeout(envMap[result.getTestClass().getRealClass()].id)
        }

        onTestFinish(result)
        taskResultMap.remove(result)  // memory
    }

    @Override
    void onTestSkipped(ITestResult result) {
        resolveResult(result)
        super.onTestSkipped(result)
        onTestFinish(result)
    }

    @Override
    void onTestStart(ITestResult result) {
        super.onTestStart(result)
        taskResultMap[result] = []
    }

    /**
     * Instead of simply delegating to the TestListenerAdapter, we first check
     * if the tasks corresponding to the test method were all OK. If not, mark
     * the results as failed, and report failure to the adapter.
     *
     * This logic is housed here for convenience, because we don't have many
     * opportunities to alter the results status. Assuming this listener
     * implementation precedes all others in the list of listeners maintained
     * by the TestNG instance, the desired status should be propagated
     * correctly.
     *
     * @param result
     */
    @Override
    void onTestSuccess(ITestResult result) {
        for (taskResult in taskResultMap[result]) {
            if (taskResult.getStatus() != ITaskResult.STATUS_OK) {
                result.setStatus(ITestResult.FAILURE)
                // move the results from the passed bin to the failed bin. We
                // must access a private field m_map in order to perform the
                // initial removal. Luckily, we can use the ITestResult object
                // passed to the current method as the key into this map, as it
                // is shared across the listeners notified of the test success
                // event.
                for (context in getTestContexts()) {
                    def passedMap = context.getPassedTests().m_map

                    if (passedMap.containsKey(result)) {
                        result.status
                        def method = passedMap.remove(result)
                        context.getFailedTests().addResult(result, method)
                        break
                    }
                }

                onTestFailure(result)
                return
            }
        }

        cb.maybeResetTimeout(envMap[result.getTestClass().getRealClass()].id)

        resolveResult(result)
        super.onTestSuccess(result)
        onTestFinish(result)
        taskResultMap.remove(result)  // memory
    }

    /**
     * Records test run information, updates testng-results.xml, and invokes
     * the report for all registered ILightestReporter's . The XML file is
     * guaranteed to be created before the other reporters are invoked, and
     * exist during their invocation (unless they delete it! or the reporter is
     * "scheduled"). Override this to add functionality that is common to test
     * failures, successes, and skips. Make sure to invoke the superclass'
     * method afterwards.
     *
     * @param result
     */
    void onTestFinish(ITestResult result) {
        def lightestResult = new LightestTestResult(result,
                                                    registry.getId(result))

        lightestResult.env = envMap[result.testClass.realClass]
        lightestResult.taskResults = taskResultMap[result]

        registry.register(lightestResult.env, result)

        List<ITestContext> contexts = getTestContexts()
        Set<ISuite> uniqueSuites = new LinkedHashSet<ISuite>()
        List<ISuite> suites = []
        List<XmlSuite> xmlSuites = []

        contexts.each {
            uniqueSuites << it.getSuite()
        }

        // the ordering of the Lists of ISuite and XmlSuite objects are
        // expected to correspond to each other

        uniqueSuites.each {
            suites << it
            xmlSuites << it.getXmlSuite()
        }

        // this method may be called by multiple threads, so synchronize
        // reporter method invocations
        synchronized (reporterInvoker) {
            reporterInvoker.updateReport(lightestResult, registry, xmlSuites,
                                         suites, outputDir)
        }
    }

    /**
     * Resolves the results in the registry. This method should be called either
     * before or atomically with an update to this adapter's passed, failed, or
     * skipped test listings; otherwise the registry is more likely to be out
     * of sync with the recorded results at time of report generation.
     *
     * @param result
     */
    protected void resolveResult(ITestResult result) {
        def id = resultCounter.getAndIncrement()
        registry.resolve(testngContext.get(), result, id)
    }

    /**
     * Adds the ITaskResult to the list kept for each ITestResult. Only root-
     * level results are added to the list; child results are linked through
     * the root.
     *
     * @param taskResult
     */
    void onTaskComplete(ITaskResult taskResult, ITestResult testResult) {
        if (taskResult.parent == null) {
            taskResultMap[testResult] << taskResult
        }
    }

    /**
     * Invokes generateReport() on all registered IReporter reporters. The 
     * testng-results.xml and testng-failed.xml XML files are guaranteed to be
     * generated before the other reports are generated. This method will be
     * invoked by the TestNG engine at the end of the suite run.
     *
     * @param xmlSuites
     * @param suites
     * @param outputDirectory this is ignored; the current value of outputDir
     *                         on this object is used instead
     */
    void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                        String outputDirectory) {
        reporterInvoker.setRegistry(registry)
        reporterInvoker.generateReport(xmlSuites, suites, outputDirectory)
    }

    void registerReporter(Object reporter) {
        reporterInvoker.registerReporter(reporter)
    }

    void initializeReporters(String configText) {
        reporterInvoker.setConfigText(configText)
        reporterInvoker.generateBaseReport(registry, outputDir)
    }

    /**
     * Throws a SkipException causing the test method execution to be skipped
     * if the test class instance is not assigned to a dispatcher. This check
     * is performed for test methods, i.e. not configuration methods. This
     * fixes issue 5; throwing the SkipException at this point instead of in
     * an inherited Before*() method avoids the problem of skipping other
     * tests that depend on the configuration method. */
    void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

        ITestNGMethod testMethod = method.getTestMethod()
        ITestClass testClass = testMethod.getTestClass()

        def methodName = testMethod.getMethodName()
        def testInstance = testClass.getInstances(true)[0]

        // avoid isConfigurationMethod(); raised TESTNG-352
        if (!method.isTestMethod()) {
            def frameworkMethodNames = testInstance.getFrameworkMethodNames()

            if (frameworkMethodNames.contains(methodName)) {
                // we never throw SkipExceptions in framework configuration
                // methods! We can freely throw them in non-framework
                // configuration methods, however.
                return
            }
        }

        //need CB check here to check CB in between test methods
        if (!testInstance.getDispatcher() || !cb.isClosed()) {
            throw new SkipException(
                    "Skipping ${methodName} because " + "${testInstance.class.name} is not assigned to a suitable " + 'dispatcher.')
        }

    }

    void afterInvocation(IInvokedMethod method, ITestResult testResult) {}
}

