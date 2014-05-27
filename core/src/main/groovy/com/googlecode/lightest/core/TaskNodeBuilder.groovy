package com.googlecode.lightest.core

/**
 * Exactly like NodeBuilder, except TaskNode's are used instead of plain old
 * Node's.*/
class TaskNodeBuilder extends BuilderSupport implements ITaskProvider {

    static TaskNodeBuilder newInstance() {
        return new TaskNodeBuilder()
    }

    protected void setParent(Object parent, Object child) {}

    protected Object createNode(Object name) {
        return new TaskNode(getCurrentNode(), name, new ArrayList())
    }

    protected Object createNode(Object name, Object value) {
        return new TaskNode(getCurrentNode(), name, value)
    }

    protected Object createNode(Object name, Map attributes) {
        return new TaskNode(getCurrentNode(), name, attributes, new ArrayList())
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        return new TaskNode(getCurrentNode(), name, attributes, value)
    }

    protected TaskNode getCurrentNode() {
        return (TaskNode) getCurrent()
    }

    TaskNodeBuilder getBuilder() {
        return this
    }

    /**
     * Returns true if the builder currently has a node, indicating we are
     * executing inside a closure context of the builder, and false otherwise.*/
    boolean hasCurrentNode() {
        return getCurrentNode() != null
    }
}
