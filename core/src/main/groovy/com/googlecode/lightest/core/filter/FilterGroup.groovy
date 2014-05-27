package com.googlecode.lightest.core.filter

import java.lang.reflect.Method

/**
 * Represents a parenthetical grouping of the inner filter.*/
class FilterGroup implements ITestFilter {
    private filter

    FilterGroup(ITestFilter filter) {
        this.filter = filter
    }

    boolean test(Method m) {
        return filter.test(m)
    }

    String filterString() {
        return '(' + filter.filterString() + ')'
    }
}
