package com.googlecode.lightest.core.tutorial

class CuilSearch implements SearchEngine {
    String url = 'http://www.cuil.com'
    String searchLocator = 'name=q'
    String submitLocator = "xpath=/descendant::button[@title='Search']"
    
    String getResultEntryLocator(String startsWithURL) {
        return "xpath=/descendant::div[@class='results']/descendant::a[starts-with(@href, '${startsWithURL}')]"
    }
}
