package com.googlecode.lightest.core.filter

import java.lang.reflect.Method

/**
 * This class represents criteria to use for selecting a set of test methods to
 * run. Implementations should choose an aspect to filter across, for example
 * a declared method annotation.
 *
 * @author hchai
 *
 */
interface ITestFilter {

    /**
     * Returns true if the method passes the filter, and false otherwise.
     *
     * @param m the method being filtered
     */
    boolean test(Method m)

    /**
     * Returns a canonical String representation of the filter.*/
    String filterString()
}