package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * Applies the composite pattern to a group of reporters.*/
class ReporterInvoker extends LightestReporter implements IReporter,
        ITestRegistryAcceptor {

    TestRegistry registry

    private SystemWrapper system
    private Set<ILightestReporter> reporters
    private Map<ILightestReporter, Integer> suiteCooldownTimeMap
    private Map<ILightestReporter, Integer> resultCooldownTimeMap

    ReporterInvoker() {
        this(new SystemWrapper())
    }

    ReporterInvoker(SystemWrapper system) {
        this.system = system
        this.reporters = new LinkedHashSet<ILightestReporter>()
        this.suiteCooldownTimeMap = [:].asSynchronized()
        this.resultCooldownTimeMap = [:].asSynchronized()
    }

    /**
     * @param reporter may additionally implement IReporter
     */
    void registerReporter(ILightestReporter reporter) {
        reporters << reporter
    }

    protected void scheduleUpdate(ILightestReporter reporter, ISuite suite,
                                  TestRegistry testRegistry, String outputDir) {
        if (reporter.getCooldownFactor() < 1) {
            // no cooldown, just run the report
            reporter.updateReport(suite, testRegistry, outputDir)
        } else if (!suiteCooldownTimeMap.containsKey(reporter)) {
            runUpdateAndScheduleCooldown(reporter, suite, testRegistry,
                                         outputDir)
        } else {
            if (system.currentTimeMillis() >= suiteCooldownTimeMap[reporter]) {
                runUpdateAndScheduleCooldown(reporter, suite, testRegistry,
                                             outputDir)
            } else {
                reporter.onUpdateDeferred()
            }
        }
    }

    protected void scheduleUpdate(ILightestReporter reporter,
                                  LightestTestResult result, TestRegistry testRegistry,
                                  List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDir) {
        if (reporter.getCooldownFactor() < 1) {
            // no cooldown, just run the report
            reporter.updateReport(result, testRegistry, xmlSuites, suites,
                                  outputDir)
        } else if (!resultCooldownTimeMap.containsKey(reporter)) {
            runUpdateAndScheduleCooldown(reporter, result, testRegistry,
                                         xmlSuites, suites, outputDir)
        } else {
            if (system.currentTimeMillis() >= resultCooldownTimeMap[reporter]) {
                runUpdateAndScheduleCooldown(reporter, result, testRegistry,
                                             xmlSuites, suites, outputDir)
            } else {
                reporter.onUpdateDeferred()
            }
        }
    }

    /**
     * Runs the reporter's update method, and updates the scheduled cooldown
     * mapping to reflect the next time this method is allowed to run. The
     * cooldown time adapts as a factor of the runtime of the report update.
     *
     * @param reporter
     * @param suite
     * @param result
     * @param testRegistry
     * @param outputDir
     */
    protected void runUpdateAndScheduleCooldown(ILightestReporter reporter,
                                                ISuite suite, TestRegistry testRegistry, String outputDir) {

        def startTime = system.currentTimeMillis()

        reporter.updateReport(suite, testRegistry, outputDir)

        def endTime = system.currentTimeMillis()
        def runTime = endTime - startTime
        def nextScheduledTime = endTime + (runTime * reporter.cooldownFactor)

        suiteCooldownTimeMap[reporter] = nextScheduledTime
    }

    protected void runUpdateAndScheduleCooldown(ILightestReporter reporter,
                                                LightestTestResult result, TestRegistry testRegistry,
                                                List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDir) {
        def startTime = system.currentTimeMillis()

        reporter.updateReport(result, testRegistry, xmlSuites, suites,
                              outputDir)

        def endTime = system.currentTimeMillis()
        def runTime = endTime - startTime
        def nextScheduledTime = endTime + (runTime * reporter.cooldownFactor)

        resultCooldownTimeMap[reporter] = nextScheduledTime
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setConfigText(String configText) {
        reporters.each { it.setConfigText(configText) }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void generateBaseReport(TestRegistry testRegistry, String outputDir) {
        reporters.each { it.generateBaseReport(testRegistry, outputDir) }
    }

    /**
     * Scheduled ILightestReporter's are treated differently here. Their
     * report generation may be deferred.*/
    @Override
    void updateReport(ISuite suite, TestRegistry testRegistry, String outputDir) {
        reporters.each {
            if (it.isScheduled()) {
                scheduleUpdate(it, suite, testRegistry, outputDir)
            } else {
                it.updateReport(suite, testRegistry, outputDir)
            }
        }
    }

    /**
     * Scheduled IScheduledReporter's are treated differently here. Their
     * report generation may be deferred.*/
    @Override
    void updateReport(LightestTestResult result, TestRegistry testRegistry,
                      List<XmlSuite> xmlSuites, List<ISuite> suites,
                      String outputDir) {

        reporters.each {
            if (it.isScheduled()) {
                scheduleUpdate(it, result, testRegistry, xmlSuites, suites,
                               outputDir)
            } else {
                it.updateReport(result, testRegistry, xmlSuites, suites,
                                outputDir)
            }
        }
    }

    void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                        String outputDirectory) {
        reporters.findAll { it instanceof IReporter }.each {
            if (it instanceof ITestRegistryAcceptor) {
                it.setRegistry(registry)
            }

            it.generateReport(xmlSuites, suites, outputDirectory)
        }
    }
}
