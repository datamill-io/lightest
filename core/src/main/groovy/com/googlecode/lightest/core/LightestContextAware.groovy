package com.googlecode.lightest.core

/**
 * Subclasses can provide all necessary contextual information for running a
 * Lightest test.*/
class LightestContextAware {
    ITaskDispatcher dispatcher

    /**
     * Returns the thread context as provided by the dispatcher. If the
     * dispatcher is not set (or is null), returns null.*/
    LightestContext getContext() {
        return dispatcher?.getContext()
    }

    /**
     * Returns the thread preferences from the context provided by the
     * dispatcher, which must be set. Otherwise, returns null.*/
    IPreferences getPrefs() {
        return getContext()?.getPrefs()
    }

    /**
     * Returns the thread environment from the context provided by the
     * dispatcher, which must be set. Otherwise, returns null.*/
    ITestEnvironment getEnv() {
        return getContext()?.getEnv()
    }
}

