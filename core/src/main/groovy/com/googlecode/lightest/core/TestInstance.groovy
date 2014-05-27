package com.googlecode.lightest.core
/**
 * Represents a single, possibly-future, TestNG test method invocation.*/
class TestInstance implements Serializable{
    private static final long serialVersionUID = 1L

    String suiteName
    String testName
    String className
    String methodName
    String methodSignature

    TestInstance(String suiteName, String testName, String className,
                 String methodName, String methodSignature) {
        this.suiteName = suiteName
        this.testName = testName
        this.className = className
        this.methodName = methodName
        this.methodSignature = methodSignature
    }

    TestInstance(TestInstance other) {
        this.suiteName = other.suiteName
        this.testName = other.testName
        this.className = other.className
        this.methodName = other.methodName
        this.methodSignature = other.methodSignature
    }

    @Override
    String toString() {
        def fields = [suiteName,
                      testName,
                      className,
                      methodName,
                      methodSignature]

        return '[' + fields.join(':') + ']'
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        TestInstance that = (TestInstance) o

        if (className != that.className) return false
        if (methodName != that.methodName) return false
        if (methodSignature != that.methodSignature) return false
        if (suiteName != that.suiteName) return false
        if (testName != that.testName) return false

        return true
    }

    int hashCode() {
        int result
        result = (suiteName != null ? suiteName.hashCode() : 0)
        result = 31 * result + (testName != null ? testName.hashCode() : 0)
        result = 31 * result + (className != null ? className.hashCode() : 0)
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0)
        result = 31 * result + (methodSignature != null ? methodSignature.hashCode() : 0)
        return result
    }
}

