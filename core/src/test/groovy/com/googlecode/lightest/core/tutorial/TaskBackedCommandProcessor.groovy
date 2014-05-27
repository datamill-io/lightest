package com.googlecode.lightest.core.tutorial

import com.googlecode.lightest.core.ITaskProvider
import com.googlecode.lightest.core.LightestTestCase
import com.googlecode.lightest.core.LightestTask
import com.thoughtworks.selenium.HttpCommandProcessor
import com.googlecode.lightest.core.LightestContext

/**
 * Produces Lightest tasks that send commands to the server, instead of doing
 * so directly. This example implementation borrows most functionality from the
 * parent class for convenience, and DOES NOT consistently override all
 * inherited methods.
 * 
 * setContext() must be called before any of start(), doCommand(), or stop()
 * to set the object that is used to fetch the task provider for creating the
 * API tasks OpenSelenium, DoSelenium, and CloseSelenium, respectively.
 */
public class TaskBackedCommandProcessor extends HttpCommandProcessor {
    LightestContext context
    
    private browserStartCommand
    private browserURL

    /**
     * @param browserStartCommand  this value is fed to the OpenSelenium task.
     *                             Even if null, the task should obtain an
     *                             appropriate default from the environment.
     * @param browserURL
     */
    TaskBackedCommandProcessor(String browserStartCommand, String browserURL) {
        // we never actually invoke the superclass' doCommand()
        super('localhost', 4444, browserStartCommand, browserURL)
        
        this.browserStartCommand = browserStartCommand
        this.browserURL = browserURL
    }
    
    @Override
    String doCommand(String command, String[] args) {
        def target = (args.length > 0) ? args[0] : ""
        def value = (args.length > 1) ? args[1] : ""
        def result = getTaskProvider().DoSelenium (command: command,
            target: target, value: value)
         
        return result.getMessage()
    }

    @Override
    void start() {
        getTaskProvider().OpenSelenium (
            browserStartCommand: browserStartCommand, browserURL: browserURL)
    }
    
    @Override
    void stop() {
        getTaskProvider().CloseSelenium ()
    }
    
    private ITaskProvider getTaskProvider() {
        assert context != null
        return context.getTaskProvider()
    }
}
