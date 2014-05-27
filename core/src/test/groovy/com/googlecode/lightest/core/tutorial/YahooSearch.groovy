package com.googlecode.lightest.core.tutorial

class YahooSearch implements SearchEngine {
    String url = 'http://www.yahoo.com'
    String searchLocator = 'id=p'
    String submitLocator = 'id=searchsubmit'
    
    String getResultEntryLocator(String startsWithURL) {
        return "xpath=id('web')/descendant::a[starts-with(@href, '${startsWithURL}')]"
    }
}

