package com.googlecode.lightest.core.filter

/**
 * Filters based on annotation value contents as defined by contains(), or
 * indexOf() in the case of Strings.*/
class AnnotationContainsFilter extends AnnotationFilter {

    boolean compare(value, attributeValue) {
        if (attributeValue instanceof String) {
            return attributeValue?.indexOf(value) != -1
        }

        // TODO - what other types, if any, should we handle?

        return attributeValue?.toList().contains(value)
    }

    String filterString() {
        return "@${field} =~ \"${value}\""
    }
}