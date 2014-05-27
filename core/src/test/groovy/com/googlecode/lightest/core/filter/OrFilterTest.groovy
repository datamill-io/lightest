package com.googlecode.lightest.core.filter

class OrFilterTest extends GroovyTestCase {
    
    void testFilterTest() {
        def trueFilter = [ test: { m -> return true } ] as ITestFilter
        def falseFilter = [ test: { m -> return false } ] as ITestFilter
        
        assertTrue(new OrFilter(trueFilter, trueFilter).test(null))
        assertTrue(new OrFilter(trueFilter, falseFilter).test(null))
        assertTrue(new OrFilter(falseFilter, trueFilter).test(null))
        assertFalse(new OrFilter(falseFilter, falseFilter).test(null))
    }
}