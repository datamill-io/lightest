package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

class GoodbyeWorld extends LightestTask {
    
    void doPerform(ITaskResult result) {
        def lastWords = (config.'@lastWords'
            ? "So long, and ${config.'@lastWords'}!" : 'Sayonara, sucker!')
        
        println lastWords
        result.setMessage("*waved* and said: ${lastWords}")
    }
}
