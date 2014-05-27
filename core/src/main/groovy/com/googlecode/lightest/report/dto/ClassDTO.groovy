package com.googlecode.lightest.report.dto

import org.testng.IClass

class ClassDTO implements IClass, Serializable {
    String name

    ClassDTO(IClass c){
        this.name = c.name
    }
    //unused

    @Override
    String getTestName() {
        return null
    }

    @Override
    Class getRealClass() {
        return null
    }

    @Override
    Object[] getInstances(boolean b) {
        return new Object[0]
    }

    @Override
    int getInstanceCount() {
        return 0
    }

    @Override
    long[] getInstanceHashCodes() {
        return new long[0]
    }

    @Override
    void addInstance(Object o) {

    }
}
