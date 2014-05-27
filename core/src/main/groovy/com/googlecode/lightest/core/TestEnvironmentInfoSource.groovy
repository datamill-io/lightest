package com.googlecode.lightest.core

/**
 * The info source class used by the DefaultReporter to populate environment
 * information into the report.*/
class TestEnvironmentInfoSource implements IReporterInfoSource {
    public static final String TYPE_NAME = 'env'

    String title
    List<String> headers
    Map<String, String> data

    TestEnvironmentInfoSource() {
        headers = ['Setting', 'Value']
        data = [:]
    }

    String getTypeName() {
        return TYPE_NAME
    }

    void configure(LightestTestResult result) {
        if (result.env) {
            title = "Environment : ${result.env.id}"

            for (setting in result.env.settings()) {
                data[setting.key] = setting.value
            }
        } else {
            title = "Environment : unassigned"
        }
    }
}