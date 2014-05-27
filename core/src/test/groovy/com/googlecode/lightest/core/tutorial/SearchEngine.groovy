package com.googlecode.lightest.core.tutorial

interface SearchEngine {
    
    /**
     * Returns the URL String where the search can be initiated.
     */
    String getUrl()
    
    /**
     * Returns the Selenium locator for the search box, which can be typed
     * into.
     */
    String getSearchLocator()
    
    /**
     * Returns the Selenium locator for the search submission element, which
     * can be clicked.
     */
    String getSubmitLocator()
    
    /**
     * Returns the Selenium locator for all entries on the search result page
     * whose links point to a URL that starts with the given String.
     *
     * @param startsWithURL  the URL the result links should start with
     */
    String getResultEntryLocator(String startsWithURL)
}
