package com.googlecode.lightest.core

import org.testng.reporters.XMLReporterConfig

/**
 * This testcase class contains helper methods for invoking the runner and
 * analyzing the results of the run.
 */
abstract class RunnerTestCase extends GroovyTestCase {
    public static final String TEST_PASSED = XMLReporterConfig.TEST_PASSED
    public static final String TEST_FAILED = XMLReporterConfig.TEST_FAILED
    public static final String TEST_SKIPPED = XMLReporterConfig.TEST_SKIPPED
    public static final String TASK_OK = "${ITaskResult.STATUS_OK}"
    public static final String TASK_FAILED = "${ITaskResult.STATUS_FAILED}"
    public static final String TASK_DOOMED = "${ITaskResult.STATUS_DOOMED}"
    
    private static final String FEATURE_DTD_VALIDATION =
        'http://apache.org/xml/features/nonvalidating/load-external-dtd'

    private static final String FEATURE_DOCTYPE_VALIDATION =
        'http://apache.org/xml/features/disallow-doctype-decl'
    
    protected RunnerSupport runnerSupport
    
    @Override
    void setUp() {
        super.setUp()
        runnerSupport = new RunnerSupport()
    }
    
    static void assertPrefix(prefix, value) {
        assertEquals(prefix, value.substring(0, prefix.length()))
    }
    
    static void assertSuffix(suffix, value) {
        assertEquals(suffix, value.substring(value.length() - suffix.length()))
    }
    
    static getTestMethods(Node root) {
        return root.depthFirst().findAll { node ->
            node.name() == 'test-method' && node.'@is-config' != 'true'
        }
    }
    
    static getTestOrConfigurationMethods(Node root) {
        return root.depthFirst().findAll { node ->
            node.name() == 'test-method'
        }
    }
    
    static getTestMethod(Node root, String methodName) {
        return getTestMethods(root).find { node ->
            node.'@name' == methodName
        }
    }
    
    static getTestOrConfigurationMethod(Node root, String methodName) {
        return getTestOrConfigurationMethods(root).find { node ->
            node.'@name' == methodName
        }
    }
    
    static getPendingTestMethod(Node root, String methodName) {
        return root.depthFirst().find { node ->
            (
                node.name() == LightestSuiteResultWriter.TAG_TEST_PENDING &&
                node.'@name' == methodName
            )
        }
    }
    
    static getFailedFile() {
        return new File(RunnerSupport.OUTPUT_DIR, RunnerSupport.FAILED_XML)
    }
    
    static getFailedMethods() {
        def failedFile = getFailedFile()
        
        assertTrue(failedFile.exists())
        
        def root = getNonValidatingParser().parse(failedFile)
        
        return root.test[0].classes[0].class[0].methods[0].include
    }
    
    static XmlParser getNonValidatingParser() {
        def parser = new XmlParser(false, true)
        parser.setFeature(FEATURE_DTD_VALIDATION, false)
        parser.setFeature(FEATURE_DOCTYPE_VALIDATION, false)
        return parser
    }
}
