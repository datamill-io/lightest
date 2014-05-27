package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.LightestTask
import com.googlecode.lightest.core.ITaskResult
import com.googlecode.lightest.core.tutorial.GoogleSearch
import com.googlecode.lightest.core.tutorial.SearchEngine

/**
 * Asserts that a given site is ranked on the first results page for a given
 * search term and search engine. Opens a new Selenium instance for this
 * purpose, and closes it when done.
 */
class AssertResultRanked extends LightestTask {
    
    /**
     * Requires:
     *   searchTerm
     *   siteURL
     *
     * Optional:
     *   searchEngine (defaults to instance of GoogleSearch)
     *   browserStartCommand
     */
    void doPerform(ITaskResult result) {
        assert config.'@searchTerm' != null
        assert config.'@siteURL' != null
        
        def searchTerm = config.'@searchTerm'
        def siteURL = config.'@siteURL'
        SearchEngine engine = config.'@searchEngine' ?: new GoogleSearch()
        
        OpenSelenium (browserStartCommand: config.'@browserStartCommand', browserURL: engine.getUrl())
        DoSelenium (command: 'open', target: '/')
        DoSelenium (command: 'type', target: engine.getSearchLocator(), value: searchTerm)
        DoSelenium (command: 'clickAndWait', target: engine.getSubmitLocator())
        DoSelenium (command: 'verifyElementPresent', target: engine.getResultEntryLocator(siteURL))
        CloseSelenium ()
    }
}