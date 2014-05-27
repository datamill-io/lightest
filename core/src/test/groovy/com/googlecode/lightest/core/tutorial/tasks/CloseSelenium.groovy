package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

import com.thoughtworks.selenium.HttpCommandProcessor

class CloseSelenium extends LightestTask {
    
    /**
     * Optional:
     *   label (defaults to "selenium")
     */
    void doPerform(ITaskResult result) {
        def label = config.'@label' ?: 'selenium'
        
        if (context[label] instanceof HttpCommandProcessor) {
            context[label].stop()
        }
        
        context[label] = null
    }
}
