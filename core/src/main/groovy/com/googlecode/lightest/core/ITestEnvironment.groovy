package com.googlecode.lightest.core

interface ITestEnvironment extends Cloneable, Serializable {

    void setId(String id)

    /**
     * Returns a unique identifier for the environment*/
    String getId()

    /**
     * Returns a map of setting names to their values. This collection excludes
     * Groovy MOP properties, as well as the id. This method name is not
     * prefixed with "get" to make the implementation easier.*/
    Map<String, Object> settings()
}
