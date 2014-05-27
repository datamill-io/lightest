package com.googlecode.lightest.core.filter

import java.lang.reflect.Method

/**
 * Filters based on annotations declared on the test method.*/
abstract class AnnotationFilter extends ComparisonFilter {

    AnnotationFilter() {}

    @Override
    void setField(Object field) {
        assert field instanceof AnnotationField

        super.setField(field)
    }

    /**
     * The name is keyed off the UNQUALIFIED class name of an annotation type
     * present on the method. If the annotation is not present, this method
     * returns null. If the member of the annotation field is specified, that
     * member value (which must be a String) is used. Otherwise, value() (which
     * also must be a String) will be used.
     *
     * This method may return a String or String[].
     *
     * @param field
     * @param m
     */
    def getAttributeValue(field, Method m) {
        def annotation = m.getAnnotations().find {
            it.annotationType().name.split(/\./)[-1] == field.name
        }

        if (!annotation) {
            return null
        }

        if (field.member) {
            try {
                return annotation."${field.member}"()
            } catch (e) {
                return null
            }
        }

        return annotation.value()
    }
}