package com.googlecode.lightest.core.filter

class AndFilterTest extends GroovyTestCase {
    
    void testFilterTest() {
        def trueFilter = [ test: { m -> return true } ] as ITestFilter
        def falseFilter = [ test: { m -> return false } ] as ITestFilter
        
        assertTrue(new AndFilter(trueFilter, trueFilter).test(null))
        assertFalse(new AndFilter(trueFilter, falseFilter).test(null))
        assertFalse(new AndFilter(falseFilter, trueFilter).test(null))
        assertFalse(new AndFilter(falseFilter, falseFilter).test(null))
    }
}