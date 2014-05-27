package com.googlecode.lightest.core

/**
 * Wraps static method calls on System to achieve DI.*/
public class SystemWrapper {

    long currentTimeMillis() {
        return System.currentTimeMillis()
    }

    String getProperty(String key) {
        return System.getProperty(key)
    }
}
