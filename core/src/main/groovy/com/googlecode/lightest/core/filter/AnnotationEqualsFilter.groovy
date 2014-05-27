package com.googlecode.lightest.core.filter

/**
 * Filters based on annotation value equality.*/
class AnnotationEqualsFilter extends AnnotationFilter {

    boolean compare(value, attributeValue) {
        return attributeValue == value
    }

    String filterString() {
        return "@${field} == \"${value}\""
    }
}