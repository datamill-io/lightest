package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

class QueryWorld extends LightestTask {
    
    void doPerform(ITaskResult result) {
        result.setMessage("Current World: ${env.getWorld()}")
    }
}