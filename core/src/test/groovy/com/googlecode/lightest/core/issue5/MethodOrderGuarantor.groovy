package com.googlecode.lightest.core.issue5

import org.testng.IMethodInstance 
import org.testng.IMethodInterceptor
import org.testng.ITestContext 

class MethodOrderGuarantor implements IMethodInterceptor {
    /**
     * the name of the method guaranteed to be run first
     */
    String firstMethodName
    
    List<IMethodInstance> intercept(List<IMethodInstance> methods,
            ITestContext context)
    {
        def firstMethod = methods.find {
            it.getMethod().getMethodName() == firstMethodName
        }
        
        if (firstMethod && ! methods[0].is(firstMethod)) {
            methods.remove(firstMethod)
            methods.add(0, firstMethod)
        }
        
        return methods
    }
}