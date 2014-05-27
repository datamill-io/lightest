package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

class DoSelenium extends LightestTask {
    
    /**
     * Requires:
     *   command
     *   target
     *
     * Optional:
     *   value (defaults to "")
     *   label (defaults to "selenium")
     */
    void doPerform(ITaskResult result) {
        assert config.'@command' != null
        assert config.'@target' != null
        
        def command = config.'@command'
        def target = config.'@target'
        def value = config.'@value' ?: ""
        def label = config.'@label' ?: 'selenium'
        def args = (String[]) [ target, value ]
        
        result.setMessage(context[label].doCommand(command, args))
    }
}
