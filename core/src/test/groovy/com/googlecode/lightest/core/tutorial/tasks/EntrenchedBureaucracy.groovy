package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

class EntrenchedBureaucracy extends LightestTask {

    void doPerform(ITaskResult result) {
        def monies = context.'monies'
        
        if (monies > 0) {
            context.'monies' = monies - 1
            result.setMessage("\$${context.monies} to go around...")
        }
        else {
            result.fail()
            result.setMessage("Ran out of money!")
        }
    }
}