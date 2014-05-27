package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

/**
 * An IReporter decorator that 1) enables generateReport() to be invoked as
 * an update, rather than a one-time call; and 2) enables the update to be
 * scheduled with a cooldown, rather than always invoked immediately when the
 * update method is called.*/
class LightestReporterAdapter extends LightestReporter implements IReporter,
        ITestRegistryAcceptor {

    TestRegistry registry

    /**
     * whether updateReport() will invoke generateReport() on the delegate.
     * Defaults to false.*/
    boolean updateEnabled

    @Delegate
    private IReporter delegate

    LightestReporterAdapter(IReporter delegate) {
        this.delegate = delegate
        this.updateEnabled = false
    }

    IReporter getDelegate() {
        return delegate
    }

    /**
     * Invokes the generateReport() method on the delegate, but only if
     * updateEnabled is true. If the delegate implements ITestRegistryAcceptor,
     * the testRegistry will be set for it.*/
    @Override
    void updateReport(LightestTestResult result, TestRegistry testRegistry,
                      List<XmlSuite> xmlSuites, List<ISuite> suites,
                      String outputDirectory) {

        if (updateEnabled) {
            if (delegate instanceof ITestRegistryAcceptor) {
                delegate.setRegistry(testRegistry)
            }

            delegate.generateReport(xmlSuites, suites, outputDirectory)
        }
    }

    @Override
    void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                        String outputDirectory) {

        // assuming that the registry has been set on the decorator by the
        // caller, we can propagate it here
        if (delegate instanceof ITestRegistryAcceptor) {
            delegate.setRegistry(registry)
        }

        delegate.generateReport(xmlSuites, suites, outputDirectory)
    }
}
