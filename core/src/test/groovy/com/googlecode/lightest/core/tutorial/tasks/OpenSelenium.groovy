package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult

import com.thoughtworks.selenium.HttpCommandProcessor

/**
 * Starts a Selenium instance, and attaches it to the context as a property
 * called "selenium". An alternate label can be provided for case when multiple
 * Selenium instances are used at the same time.
 */
class OpenSelenium extends LightestTask {
    
    /**
     * Environment:
     *   seleniumServerHost
     *   seleniumServerPort
     *   defaultBrowserStartCommand
     *
     * Requires:
     *   browserURL
     *
     * Optional:
     *   browserStartCommand (defaults to env.defaultBrowserStartCommand)
     *   label (defaults to "selenium")
     */
    void doPerform(ITaskResult result) {
        assert env.seleniumServerHost != null
        assert env.seleniumServerPort != null
        assert config.'@browserURL' != null
        
        def browserStartCommand = (config.'@browserStartCommand'
            ?: env.defaultBrowserStartCommand)
        def label = config.'@label' ?: 'selenium'

        assert browserStartCommand != null

        def selenium = new HttpCommandProcessor(
            env.seleniumServerHost,
            env.seleniumServerPort,
            browserStartCommand,
            config.'@browserURL')
        
        selenium.start()
        
        result.setMessage('Successfully opened browser')
        
        // close an existing labeled instance, if any
        
        if (context[label] instanceof HttpCommandProcessor) {
            context[label].stop()
        }
        
        context[label] = selenium
    }
}