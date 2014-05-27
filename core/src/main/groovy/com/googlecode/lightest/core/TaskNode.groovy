package com.googlecode.lightest.core

/**
 * The behavior is TaskNode differs from Node in that children() will never
 * include the original value specified when creating the node in the resulting
 * NodeList. That was a curious decision for the implementation of Node. This
 * original value may be obtained by using the nodeValue() method, instead of
 * value(), and may be set by passing a non-List object into setValue().
 *
 * This class is (hopefully) thread-safe.*/
class TaskNode extends Node {
    private nodeValue

    TaskNode(Node parent, Object name) {
        super(parent, name)
    }

    TaskNode(Node parent, Object name, Object value) {
        super(parent, name, (value instanceof List ? Collections.synchronizedList(value) : value))
        if (!(value instanceof List)) {
            nodeValue = value
        }
    }

    TaskNode(Node parent, Object name, Map attributes) {
        super(parent, name, attributes)
    }

    TaskNode(Node parent, Object name, Map attributes, Object value) {
        super(parent, name, attributes, (value instanceof List ? Collections.synchronizedList(value) : value))
        if (!(value instanceof List)) {
            nodeValue = value
        }
    }

    @Override
    List children() {
        return super.children().findAll { it != nodeValue }
    }

    @Override
    void setValue(Object value) {
        if (value instanceof List) {
            super.setValue(Collections.synchronizedList(value))
        } else {
            super.setValue(value)
            nodeValue = value
        }
    }

    /**
     * Returns the single, non-List value specified when creating the Node, if
     * any.*/
    def nodeValue() {
        return nodeValue
    }

    void setAttribute(String attrName, Object attrValue) {
        attributes().put(attrName, attrValue)
    }

    Object getAttribute(String attrName) {
        return attributes().get(attrName)
    }
}
