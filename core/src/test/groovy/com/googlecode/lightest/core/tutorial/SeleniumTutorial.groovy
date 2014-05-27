package com.googlecode.lightest.core.tutorial

import com.googlecode.lightest.core.LightestTestCase
import com.googlecode.lightest.core.SimpleApi
import org.testng.annotations.*

class SeleniumTutorial extends LightestTestCase {

    SeleniumTutorial() {
        def api = new SimpleApi('com.googlecode.lightest.core.tutorial.tasks')
        setApi(api)
    }
    
    @Test
    void lightestProjectPageIsRanked() {
        def engines = [
            new GoogleSearch(),
            new LiveSearch(),
            //new YahooSearch(),
            new CuilSearch()
        ]
        
        for (engine in engines) {
            AssertResultRanked (searchTerm: 'lightest test', siteURL: 'http://code.google.com/p/lightest', searchEngine: engine)
        }
    }
    
    @Test
    void lightestProjectPageIsRanked2() {
        def engines = [
            new GoogleSearch(),
            new LiveSearch(),
            //new YahooSearch(),
            new CuilSearch()
        ]
        
        for (engine in engines) {
            AssertResultRanked2 (searchTerm: 'lightest test', siteURL: 'http://code.google.com/p/lightest', searchEngine: engine)
        }
    }
}