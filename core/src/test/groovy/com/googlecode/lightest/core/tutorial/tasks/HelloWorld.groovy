package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

class HelloWorld extends LightestTask {
    String greeting = 'Hello World!'
    
    void doPerform(ITaskResult result) {
        def parentResult = result.parent
        
        if (parentResult?.getTask()?.class?.name?.endsWith('QueryWorld')) {
            greeting += " (${parentResult.getMessage()})"
        }
        
        println greeting
        result.setMessage("Said: ${greeting}")
    }
}
