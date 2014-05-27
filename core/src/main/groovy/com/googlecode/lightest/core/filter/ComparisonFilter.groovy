package com.googlecode.lightest.core.filter

import java.lang.reflect.Method

/**
 * Filters an attribute related to the test method based on a value and an
 * operation with that value. The case sensitivity of the filtering can be set.
 * Filtering is case-sensitive by default.*/
abstract class ComparisonFilter implements ITestFilter {
    /**
     * the field provides the value to be used for filtering, and will be
     * passed into getAttributeValue() .*/
    def field
    def value

    private caseSensitive

    ComparisonFilter() {
        caseSensitive = true
    }

    /**
     * Sets whether the comparison should be case sensitive for Strings. Has no
     * effect if the values being compared are not Strings.
     *
     * @param caseSensitive case-sensitive if true, not if false
     */
    void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive
    }

    boolean isCaseSensitive() {
        return caseSensitive
    }

    /**
     * Compares the test method's attribute value as obtained by
     * getAttributeValue() with the defined value, using compare() for the
     * compare operation. Case-sensitivity is determined by whether/how
     * setCaseSensitive() was invoked, and has no effect when the values being
     * compared are not Strings!
     *
     * @param m
     */
    boolean test(Method m) {
        assert field != null
        assert value != null

        def attributeValue = getAttributeValue(field, m)

        if (attributeValue == null) {
            return false
        }

        if (!caseSensitive && value instanceof String) {
            if (attributeValue instanceof List) {
                attributeValue = attributeValue.collect {
                    it instanceof String ? it.toLowerCase() : it
                }
            }

            return compare(value.toLowerCase(), attributeValue)
        }

        return compare(value, attributeValue)
    }

    /**
     * Returns true if the comparison criteria is satisfied, false otherwise.
     *
     * @param value the value to compare against
     * @param attributeValue the value being compared
     */
    abstract boolean compare(value, attributeValue)

    /**
     * Returns the method attribute identified by the given field. This should
     * be a modifiable value.
     *
     * @param field the field to get
     * @param m the method for which to retrive the attribute value
     */
    abstract getAttributeValue(field, Method m)
}