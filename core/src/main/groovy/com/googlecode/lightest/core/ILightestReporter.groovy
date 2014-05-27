package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * In contrast to the TestNG IReporter, which is intended to generate a single
 * report after all tests have finished running, this interface defines a report
 * that can be updated throughout the test run. generateBaseReport() is called
 * to initialize the report, and some flavor of updateReport() is invoked both
 * after a suite starts, and after each test finishes.
 *
 * A scheduled reporter always produces an accurate report, regardless of when
 * it is run. Other types of reporters must be run at specific times to
 * generate meaningful results. The ability to defer execution of a scheduled
 * report provides flexibility to adjust the frequency of generation in view
 * of performance considerations; reports can be quite heavy in this regard.
 * Scheduling is indicated by calling setScheduled() with true.*/
interface ILightestReporter {

    /**
     * Sets the textual configuration used to configure this run.
     *
     * @param configText the textual configuration, or null
     */
    void setConfigText(String configText)

    /**
     * Performs one-time setup activities for the report.
     *
     * @param testRegistry the registry contains all known information about
     *                      pending tests.
     * @param outputDir
     */
    void generateBaseReport(TestRegistry testRegistry, String outputDir)

    /**
     * Updates the report at the beginning of a new suite run.
     *
     * @param suite the suite that is about to run
     * @param testRegistry the registry contains all known information about
     *                      pending and completed tests
     * @param outputDir
     */
    void updateReport(ISuite suite, TestRegistry testRegistry, String outputDir)

    /**
     * Updates the report following the generation of a new test results. The
     * signature conveniently accommodates IReporter implementations.
     *
     * @param result the results for which to update the report.
     * @param testRegistry the registry contains all known information about
     *                      pending and completed tests
     * @param xmlSuites
     * @param suites
     * @param outputDirectory
     */
    void updateReport(LightestTestResult result, TestRegistry testRegistry,
                      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory)

    /**
     * Returns true if this reporter may be scheduled, false otherwise.*/
    boolean isScheduled()

    /**
     * Sets whether this reporter can be scheduled.
     *
     * @param scheduled
     */
    void setScheduled(boolean scheduled)

    /**
     * Returns a factor by which the runtime of the report should be multipled
     * by to obtain a cooldown period during which the report should not be
     * updated. Non-positive values indicate no cooldown need be observed.*/
    int getCooldownFactor()

    /**
     * This callback should be invoked whenever an update to this reporter is
     * intentionally skipped due the cooldown period not having fully elapsed.*/
    void onUpdateDeferred()
}