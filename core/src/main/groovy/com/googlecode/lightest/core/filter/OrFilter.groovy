package com.googlecode.lightest.core.filter

import java.lang.reflect.Method

/**
 * Returns the result of a logical OR on two sub-filters.*/
class OrFilter implements ITestFilter {
    private filters

    /**
     * We would use varargs, but Java doesn't like Groovy varargs.*/
    OrFilter(ITestFilter filter1, ITestFilter filter2) {
        this.filters = [filter1, filter2]
    }

    boolean test(Method m) {
        return filters.any { it.test(m) }
    }

    String filterString() {
        def s = filters.collect { it.filterString() }
        return s.join(' || ')
    }
}