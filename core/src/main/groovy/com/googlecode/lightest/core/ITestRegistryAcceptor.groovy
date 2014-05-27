package com.googlecode.lightest.core

import com.googlecode.lightest.core.TestRegistry

/**
 * This interface marks a class that is aware of a TestRegistry, mostly likely
 * using it for reporting purposes. See {@link LightestTestListener}.*/
interface ITestRegistryAcceptor {

    /**
     * Set the registry on the object.*/
    void setRegistry(TestRegistry registry)

    /**
     * Return the registry currently set on the object.*/
    TestRegistry getRegistry()
}