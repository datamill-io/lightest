package com.googlecode.lightest.report.dto

import org.testng.IResultMap
import org.testng.ITestNGMethod
import org.testng.ITestResult

class ResultMapDTO implements IResultMap, Serializable {
    private static final int serialVersionUID = 1L
    HashMap<ITestNGMethod, Set<ITestResult>> map = new HashMap<>()
    @Override
    void addResult(ITestResult iTestResult, ITestNGMethod iTestNGMethod) {
        Set<ITestResult> res = map.get(ITestNGMethod)
        if (!res){
            res = new HashSet<>()
            map.put(iTestNGMethod, res)
        }
        res.add(iTestResult)
    }

    @Override
    Set<ITestResult> getResults(ITestNGMethod iTestNGMethod) {
        return map.get(ITestNGMethod)
    }

    @Override
    Set<ITestResult> getAllResults() {
        HashSet<ITestResult> allRes = new HashSet<>()
        map.values().each { allRes.addAll(it)}
        return allRes
    }

    @Override
    Collection<ITestNGMethod> getAllMethods() {
        return map.keySet()
    }

    @Override
    int size() {
        return map.size()
    }
}
