package com.googlecode.lightest.core

/**
 * A marker interface for preferences related to the test run. ITask's may
 * query the preferences object for information necessary to configure the
 * task. Implementations should be POGO value objects composed of Strings and
 * Integers, with no additional logic. Referencing classes should use "getX()"
 * to retrieve the desired preference.*/
interface IPreferences extends Cloneable {}
