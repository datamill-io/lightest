package com.googlecode.lightest.core.filter

import java.lang.reflect.Method

/**
 * Negates the result of a wrapped filter.*/
class NotFilter implements ITestFilter {
    private filter

    NotFilter(ITestFilter filter) {
        this.filter = filter
    }

    boolean test(Method m) {
        return !filter.test(m)
    }

    String filterString() {
        return '! ' + filter.filterString()
    }
}