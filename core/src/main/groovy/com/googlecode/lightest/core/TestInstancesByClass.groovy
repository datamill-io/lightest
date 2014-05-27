package com.googlecode.lightest.core

class TestInstancesByClass implements Serializable {
    String className
    List<TestInstance> instances = []

    TestInstancesByClass(String className, List<TestInstance> ti){
        this.className = className
        ti.each {instances << it}
    }
}
