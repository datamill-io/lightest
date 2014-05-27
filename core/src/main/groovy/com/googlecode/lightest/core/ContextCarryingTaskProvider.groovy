package com.googlecode.lightest.core

/**
 * Wraps ITaskProvider with a simplified Map-like interface which supports
 * associating provider-specified context values.*/
class ContextCarryingTaskProvider {
    @Delegate
    private ITaskProvider delegate

    private Map context

    ContextCarryingTaskProvider(ITaskProvider delegate) {
        this.delegate = delegate
        this.context = [:]
    }

    void put(Object key, Object value) {
        context[key] = value
    }

    Object get(Object key) {
        return context[key]
    }

    boolean containsKey(Object key) {
        return context.containsKey(key)
    }

    Object remove(Object key) {
        return context.remove(key)
    }
}