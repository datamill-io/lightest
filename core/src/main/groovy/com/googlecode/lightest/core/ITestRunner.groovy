package com.googlecode.lightest.core

import org.testng.ITestNGListener

interface ITestRunner {

    /**
     * Returns a modifiable List of ITestNGListener objects that will be
     * registered directly with the underlying runner. Note that this List may
     * be modified / clobbered if configure() is called. */
    List<ITestNGListener> getTestNGListeners()

    /**
     * Configures the runner by wiring together dependencies as specified by
     * the configuration file.
     *
     * @param configFile
     */
    void configure(File configFile)

    /**
     * Configures the runner by wiring together dependencies as specified by
     * the configuration text.
     *
     * @param configText
     */
    void configure(String configText)

    /**
     * Executes the test suites represented by XML files, along with tests
     * represented as Groovy files, whose paths are provided as a parameter to
     * this method. The contextual class loader of the TestNG run is a
     * GroovyClassLoader that is aware of any task classpaths specified in the
     * Configuration.
     *
     * @param paths paths to files that are either Lightest suite
     *                          XML files, or Groovy scripts that are
     *                          LightestTestCase's.
     * @param interactiveStart whether to start the runner in interactive
     *                          mode, if possible      */
    void run(List<String> paths, boolean interactiveStart)

}
