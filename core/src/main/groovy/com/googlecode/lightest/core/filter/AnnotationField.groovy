package com.googlecode.lightest.core.filter

/**
 * Identifies an annotation field by name, or name and member, where the member
 * is an attribute of the annotation. For example, for the annotation
 * "@Test(groups = [ 'foo', 'bar' ])", the name is "Test" and the member is
 * "groups".*/
public class AnnotationField {
    String name
    String member

    AnnotationField(String name) {
        this(name, "")
    }

    AnnotationField(String name, String member) {
        this.name = name
        this.member = member
    }

    String toString() {
        return name + (member ? ".${member}" : "")
    }
}
