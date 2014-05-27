package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import org.testng.IReporter
import org.testng.ISuite
import org.testng.ITestResult
import org.testng.xml.XmlSuite

/**
 * @author hchai
 *
 */
class FailedReporterDecorator extends LightestReporterAdapter {
    FailedReporterDecorator(IReporter delegate) {
        super(delegate)
    }

    @Override
    void updateReport(LightestTestResult result, TestRegistry testRegistry,
                      List<XmlSuite> xmlSuites, List<ISuite> suites,
                      String outputDirectory) {

        if (result.getStatus() == ITestResult.FAILURE) {
            delegate.generateReport(xmlSuites, suites, outputDirectory)
        }
    }
}
