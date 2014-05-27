package com.googlecode.lightest.core;

import org.testng.xml.XmlSuite;

/**
 * Provides the ability to check the validity of a set of TestRunner arguments
 * before a run begins.  This can be used to extend the default checks for
 * additional validity constraints.*/
interface IRunValidator {
    /**
     * Determines if the run is valid.  If valid, return true.  If not, log
     * appropriate error messages, and return false.*/
    boolean validate(IConfiguration config, List<XmlSuite> suites)
}
