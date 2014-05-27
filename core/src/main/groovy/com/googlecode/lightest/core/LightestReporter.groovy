package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * An adapter class with no actual reporting functionality. Subclasses can
 * choose to implement any or all of the reporting-related methods.*/
class LightestReporter implements ILightestReporter {
    /** the default value for cooldownFactor */
    public static final int DEFAULT_COOLDOWN_FACTOR = 25

    String configText
    boolean scheduled
    int cooldownFactor

    /**
     * The reporter is not marked as scheduled, by default. configText is null
     * by default. */
    LightestReporter() {
        configText = null
        scheduled = false
        cooldownFactor = DEFAULT_COOLDOWN_FACTOR
    }

    /**
     * No-op.*/
    void generateBaseReport(TestRegistry testRegistry, String outputDir) {}

    /**
     * No-op.*/
    void updateReport(ISuite suite, TestRegistry testRegistry,
                      String outputDir) {}

    /**
     * No-op.*/
    void updateReport(LightestTestResult result, TestRegistry testRegistry,
                      List<XmlSuite> xmlSuites, List<ISuite> suites,
                      String outputDirectory) {}
    /**
     * No-op.*/
    void onUpdateDeferred() {}
}
