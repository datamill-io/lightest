package com.googlecode.lightest.core.tutorial

class LiveSearch implements SearchEngine {
    String url = 'http://www.live.com'
    String searchLocator = 'name=q'
    String submitLocator = 'name=go'
    
    String getResultEntryLocator(String startsWithURL) {
        return "xpath=id('results')/descendant::a[starts-with(@href, '${startsWithURL}')]"
    }
}

