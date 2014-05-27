package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.*
import com.googlecode.lightest.core.tutorial.GoogleSearch
import com.googlecode.lightest.core.tutorial.SearchEngine
import com.googlecode.lightest.core.tutorial.TaskBackedSelenium
import com.thoughtworks.selenium.DefaultSelenium

/**
 * Asserts that a given site is ranked on the first results page for a given
 * search term and search engine. Opens a new Selenium instance for this
 * purpose, and closes it when done.
 */
class AssertResultRanked2 extends LightestTask {
    
    /**
     * Requires:
     *   searchTerm
     *   siteURL
     *
     * Optional:
     *   browserStartCommand
     *   searchEngine (defaults to instance of GoogleSearch)
     */
    void doPerform(ITaskResult result) {
        assert config.'@searchTerm' != null
        assert config.'@siteURL' != null
        
        def searchTerm = config.'@searchTerm'
        def siteURL = config.'@siteURL'
        SearchEngine engine = config.'@searchEngine' ?: new GoogleSearch()
        
        def selenium = createSelenium(config.'@browserStartCommand',
            engine.getUrl())
        
        selenium.start()
        selenium.open('/')
        selenium.type(engine.getSearchLocator(), searchTerm)
        selenium.clickAndWait(engine.getSubmitLocator())
        
        def entryLocator = engine.getResultEntryLocator(siteURL)
        
        if (! selenium.isElementPresent(entryLocator)) {
            result.setMessage('The site URL was not found in the results!')
            result.fail()
        }
        
        selenium.stop()
    }
    
    /**
     * Returns a new Selenium object that creates tasks as it executes
     * commands.
     * 
     * @param browserStartCommand
     * @param browserURL
     */
    def createSelenium(browserStartCommand, browserURL) {
        def selenium = new TaskBackedSelenium(browserStartCommand, browserURL)
        
        selenium.setContext(getContext())
        
        return selenium
    }
}