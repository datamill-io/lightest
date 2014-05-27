package com.googlecode.lightest.core

import com.googlecode.lightest.core.ITestEnvironment
import com.googlecode.lightest.core.TestNGContext
import org.testng.ITestResult

/**
 * The test registry is used to keep track of two things: 1) the pending tests,
 * i.e. ones that are scheduled to run but have not yet had results reported
 * against them; and 2) the id of each test results - which is used to
 * cross-reference between the default generated testng-results.xml and the
 * LightestTestResult object passed into ILightestReporter methods. All
 * operations are thread-safe.*/
class TestRegistry {
    private Map<TestInstance, Integer> data
    private Map<ITestResult, Integer> idMap
    private Map<ITestResult, ITestEnvironment> environmentMap

    TestRegistry() {
        data = [:].asSynchronized()
        idMap = [:].asSynchronized()
        environmentMap = [:].asSynchronized()
    }

    TestRegistry(List<TestInstance> tests) {
        this()
        add(tests)
    }

    private TestRegistry(Map<TestInstance, Integer> data, Map<ITestResult, Integer> idMap) {
        this()
        synchronized (data) {
            this.data.putAll(data)
        }
        synchronized (idMap) {
            this.idMap.putAll(idMap)
        }
    }

    /**
     * Adds a single TestInstance to the registry.
     *
     * @param test the test instance to add
     */
    void add(TestInstance test) {
        synchronized (this) {
            if (!data[test]) {
                data[new TestInstance(test)] = 1
            } else {
                ++data[test]
            }
        }
    }

    /**
     * Adds a list of TestInstance's to the registry.
     *
     * @param tests the test instances to add
     */
    void add(List<TestInstance> tests) {
        tests.each { add(it) }
    }

    void resolve(TestNGContext testngContext, ITestResult result, int id) {
        resolve(testngContext.suiteName, testngContext.testName, result, id)
    }

    /**
     * Removes the first test instance associated with the TestNG test results,
     * if any, and adds in a results to replace it.
     *
     * @param suiteName
     * @param testName
     * @param result
     */
    void resolve(String suiteName, String testName, ITestResult result, int id) {
        def testMethod = result.getMethod()
        def test = new TestInstance(suiteName, testName, result.testClass.name,
                                    testMethod.methodName, testMethod.signature)

        synchronized (this) {
            // exists and is > 0
            if (data[test]) {
                --data[test]
            }
        }

        idMap[result] = id
    }

    /**
     * Returns an immutable mapping of TestInstance's to their counting tickers.
     * Ordering of entries in the map should be preserved for subsequent
     * iterations across the map.*/
    Map<TestInstance, Integer> getData() {
        return data.asImmutable()
    }

    /**
     * Returns the Integer id this results was resolved with, or null if the
     * results is not registered.
     *
     * @param result
     */
    Object getId(ITestResult result) {
        return idMap[result] ?: null
    }

    /**
     * Stores the association between an Environment and a raw TestNG ITestResult.
     *
     * @param ltr
     * @param result
     */
    void register(ITestEnvironment env, ITestResult result) {
        environmentMap[result] = env
    }



    /**
     * Returns a Set of unique class names associated with all
     * registered test instances.*/
    Set<String> getUniqueClassNames() {
        return new LinkedHashSet(data.keySet().collect { it.className })
    }

    /**
     * Returns a new test registry, filtered by suite className and test className.
     *
     * @param suiteName
     * @param testName
     */
    TestRegistry match(String suiteName, String testName) {
        def matches = data.findAll { k, v -> k.suiteName == suiteName && k.testName == testName
        }
        return new TestRegistry(matches, idMap)
    }

    /**
     * Returns a new test registry, filtered by class className.
     *
     * @param className
     */
    TestRegistry match(String className) {
        def matches = data.findAll { k, v -> k.className == className
        }
        return new TestRegistry(matches, idMap)
    }

    String toString() {
        return data.collect { k, v -> "${k}:${v}" }.join('\n')
    }

    boolean wereAllTestsRun(String testName) {
        for (Map.Entry<TestInstance, Integer> entry: data){
            if (entry.key.testName.equals(testName)){
                if (entry.value > 0) return false
            }
        }
        return true
    }
}