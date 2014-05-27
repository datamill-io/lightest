package com.googlecode.lightest.core.tutorial

class GoogleSearch implements SearchEngine {
    String url = 'http://www.google.com'
    String searchLocator = 'name=q'
    String submitLocator = 'name=btnG'
    
    String getResultEntryLocator(String startsWithURL) {
        return "xpath=id('res')/descendant::a[starts-with(@href, '${startsWithURL}')]"
    }
}

