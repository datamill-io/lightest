package com.googlecode.lightest.core.tutorial

import com.googlecode.lightest.core.LightestContext
import com.googlecode.lightest.core.TaskNodeBuilder
import com.thoughtworks.selenium.DefaultSelenium
import com.thoughtworks.selenium.Selenium

/**
 * This extension of the GroovySelenium wrapper object is specialized for
 * Lightest, and will not choke when used inside a TaskNodeBuilder closure. Its
 * command processor, a TaskBackedCommandProcessor, produces tasks for running
 * Selenium commands.
 * 
 * setContext() should be called to set the context used both to determine the
 * closure state, and for the underlying command processor to build tasks.
 */
class TaskBackedSelenium  {
    private TaskBackedCommandProcessor commandProcessor
    private LightestContext context
    private Selenium selenium
    
    TaskBackedSelenium(String browserStartCommand, String browserURL) {
        this(new TaskBackedCommandProcessor(browserStartCommand, browserURL))
    }
    
    TaskBackedSelenium(TaskBackedCommandProcessor commandProcessor) {
        this(new DefaultSelenium(commandProcessor))
        this.commandProcessor = commandProcessor
    }
    
    private TaskBackedSelenium(Selenium selenium) {
        this.selenium = selenium;
    }
    
    void setContext(LightestContext context) {
        this.context = context
        commandProcessor.setContext(context)
    }
    
    /**
     * Swallows exceptions that originate when the method is invoked inside a
     * TaskNodeBuilder closure. The underlying command processor will not yield
     * return values for such calls, which may cause its calling methods to
     * fail.
     */
    def methodMissing(String name, args) {
        try {
            return super.methodMissing(name, args)
        }
        catch (e) {
            def taskProvider = context.getTaskProvider()
            
            if (! (taskProvider instanceof TaskNodeBuilder)) {
                throw e
            }
        }
    }
}
