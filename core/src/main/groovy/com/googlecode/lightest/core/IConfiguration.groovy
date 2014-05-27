package com.googlecode.lightest.core

import org.testng.ITestNGListener
import org.testng.reporters.FailedReporter

/**
 * Represents a configuration of the test run*/
interface IConfiguration {

    /**
     * Returns the class loader that was used (or whose equivalent may be used)
     * to load the configuration-related classes.*/
    GroovyClassLoader getClassLoader()

    /**
     * Returns a List of Strings representing class paths under which tasks
     * and potentially environment and preference classes are defined. The
     * current directory is added by default.*/
    List<String> getClassPaths()

    /**
     * Returns the output directory for the test run report.*/
    String getOutputDir()

    /**
     * Returns a List of ITestEnvironment instances.*/
    List<ITestEnvironment> getEnvironments()

    /**
     * Returns an instance of the concrete implementation of IPreferences.*/
    IPreferences getPreferences()

    /**
     * Returns a list of reporter instances. They should implement either
     * org.testng.IReporter or ILightestReporter .*/
    List<ILightestReporter> getReporters()

    /**
     * Returns a list of listener instances to be registered directly with the
     * TestNG runner. These should implement {@link ITestNGListener}.*/
    List<ITestNGListener> getListeners()

    /**
     * Returns a list of run validators to be used by the {@link TestRunner}.*/
    List<IRunValidator> getRunValidators()

    /**
     * Returns the name of the class to be used as the context class. This
     * class must be or extend LightestContext.*/
    String getContextClass()

    ILightestReporter getXMLReporter()

    ILightestReporter getPendingReporter()

    ILightestReporter getFailedReporter()

    IDispatcherAssignmentStrategy getDispatcherAssignmentStrategy()

    /**
     * Returns an instance of the  ITaskDispatchStrategy implementation to use
     * instead of whatever implementation is available by default. This method
     * may return null if no custom strategy is specified.*/
    ITaskDispatchStrategy getTaskDispatchStrategy()
}
