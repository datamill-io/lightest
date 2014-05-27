package com.googlecode.lightest.core

import org.testng.internal.annotations.IAnnotationFinder
import org.testng.internal.annotations.JDK14AnnotationFinder
import org.testng.internal.ClassHelper
import org.testng.internal.MethodHelper
import org.testng.ITestClass
import org.testng.SuiteRunner
import org.testng.TestNG
import org.testng.xml.XmlSuite
import org.testng.xml.XmlTest

/**
 * This class delves into TestNG internals in order to produce a listing of
 * tests that are represented by a set of suites. Normally, TestNG discovers
 * this list as it is running the tests. For reporting purposes, we would like
 * to be able to have this information a priori, so we can generate a listing
 * of pending tests.*/
class TestInstanceFinder {
    private TestNG testng

    TestInstanceFinder() {
        this(new TestNG())
    }

    /**
     * @param parserFactory
     * @param testng the TestNG instance whose logic to use to find
     *                       test instance information
     */
    TestInstanceFinder(TestNG testng) {
        this.testng = testng
    }

    /**
     * Finds and returns all TestInstances represented by a list of suites to
     * be run.
     *
     * @param suites a List of XmlSuite's to run
     */
    TestRegistry find(List<XmlSuite> suites) {
        def registry = new TestRegistry()
        def finders = createAnnotationFinders()

        for (XmlSuite suite in suites) {
            def suiteRunner = createSuiteRunner(suite, finders)

            for (XmlTest test in suite.getTests()) {
                def testRunner = createTestRunner(suiteRunner, test)
                def runInfo = testRunner.m_runInfo

                for (ITestClass testClass in testRunner.getIClass()) {
                    for (testMethod in testClass.getTestMethods()) {
                        def method = testMethod.getMethod()

                        // take enabled status into account
                        if (!MethodHelper.isEnabled(method, finders[1])) {
                            continue
                        }

                        // take explicit inclusions / exclusions into account
                        if (!runInfo.includeMethod(testMethod, true)) {
                            continue
                        }

                        for (i in 1..testMethod.getInvocationCount()) {
                            // getSignature() is not exposed in the interface
                            registry.add(new TestInstance(suite.getName(),
                                                          test.getName(), testClass.getName(),
                                                          testMethod.getMethodName(),
                                                          testMethod.getSignature()))
                        }
                    }
                }
            }
        }

        return registry
    }

    /**
     * Finds and returns all TestInstances represented by a list of suites to
     * be run.
     *
     * @param suites a List of XmlSuite's to run
     */
    List<TestInstancesByClass> findTestInstancesGroupByClass(List<XmlSuite> suites) {
        List<TestInstancesByClass> ret = []
        def finders = createAnnotationFinders()

        for (XmlSuite suite in suites) {
            def suiteRunner = createSuiteRunner(suite, finders)

            println("suite ${suite.name}")
            for (XmlTest test in suite.getTests()) {
                println("\ttest ${test.name}")
                //XXX: THis call is fucking death.
                def testRunner = createTestRunner(suiteRunner, test)
                def runInfo = testRunner.m_runInfo

                for (ITestClass testClass in testRunner.getIClass()) {
                    println("\t\tclass ${testClass.name}")
                    def instances = []
                    for (testMethod in testClass.getTestMethods()) {
                        println("\t\t\tclass ${testMethod.methodName}")
                        def method = testMethod.getMethod()

                        // take enabled status into account
                        if (!MethodHelper.isEnabled(method, finders[1])) {
                            continue
                        }

                        // take explicit inclusions / exclusions into account
                        if (!runInfo.includeMethod(testMethod, true)) {
                            continue
                        }

                        for (i in 1..testMethod.getInvocationCount()) {
                            instances << new TestInstance(suite.getName(),
                                                          test.getName(), testClass.getName(),
                                                          testMethod.getMethodName(),
                                                          testMethod.getSignature())

                        }
                    }
                    ret << new TestInstancesByClass(testClass.name, instances)
                }
            }
        }

        return ret
    }

    public List<SuiteTracker> makeSuiteTracker(List<XmlSuite> suites) {
        def registry = new TestRegistry()
        def finders = createAnnotationFinders()
        List<SuiteTracker> suiteTrackers = []

        for (XmlSuite suite in suites) {
            def suiteRunner = createSuiteRunner(suite, finders)

            for (XmlTest test in suite.getTests()) {
                def testRunner = createTestRunner(suiteRunner, test)
                def runInfo = testRunner.m_runInfo

                for (ITestClass testClass in testRunner.getIClass()) {
                    for (testMethod in testClass.getTestMethods()) {
                        def method = testMethod.getMethod()

                        // take enabled status into account
                        if (!MethodHelper.isEnabled(method, finders[1])) {
                            continue
                        }

                        // take explicit inclusions / exclusions into account
                        if (!runInfo.includeMethod(testMethod, true)) {
                            continue
                        }

                        for (i in 1..testMethod.getInvocationCount()) {
                            // getSignature() is not exposed in the interface
                            registry.add(new TestInstance(suite.getName(),
                                                          test.getName(), testClass.getName(),
                                                          testMethod.getMethodName(),
                                                          testMethod.getSignature()))
                        }
                    }
                }
            }
            suiteTrackers << new SuiteTracker(registry, suite, suiteRunner)
        }

        return suiteTrackers
    }

    protected SuiteRunner createSuiteRunner(XmlSuite xmlSuite,
                                            IAnnotationFinder[] finders) {
        return new SuiteRunner(xmlSuite, null, finders)
    }

    protected org.testng.TestRunner createTestRunner(SuiteRunner suiteRunner,
                                                     XmlTest xmlTest) {
        def runnerFactory = suiteRunner.buildRunnerFactory(suiteRunner.m_testlisteners)
        return runnerFactory.newTestRunner(suiteRunner, xmlTest)
    }

    protected IAnnotationFinder[] createAnnotationFinders() {
        def transformer = testng.getAnnotationTransformer()

        assert transformer != null

        return [new JDK14AnnotationFinder(transformer),
                ClassHelper.createJdkAnnotationFinder(transformer)] as IAnnotationFinder[]
    }
}

