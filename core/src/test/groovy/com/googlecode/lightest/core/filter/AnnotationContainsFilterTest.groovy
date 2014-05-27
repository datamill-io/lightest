package com.googlecode.lightest.core.filter

import org.testng.annotations.Test

class AnnotationContainsFilterTest extends GroovyTestCase {
    
    @Test(groups = [ 'foo', 'bar' ])
    void annotatedMethod1() {
        // nothing doing
    }
    
    @Test(groups = [ 'bar', 'baz' ])
    void annotatedMethod2() {
        // nothing doing
    }
    
    @Test(groups = [])
    void annotatedMethod3() {
        // nothing doing
    }
    
    @Test
    void annotatedMethod4() {
        // nothing doing
    }
    
    void testFilterTest() {
        def filter = new AnnotationContainsFilter()
        def m1 = this.class.methods.find { it.name == 'annotatedMethod1' }
        def m2 = this.class.methods.find { it.name == 'annotatedMethod2' }
        def m3 = this.class.methods.find { it.name == 'annotatedMethod3' }
        def m4 = this.class.methods.find { it.name == 'annotatedMethod4' }
        
        assertNotNull(m1)
        assertNotNull(m2)
        assertNotNull(m3)
        assertNotNull(m4)
        
        filter.setField(new AnnotationField('Test', 'groups'))
        filter.setValue('foo')
        filter.setCaseSensitive(true)
        
        assertTrue(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
        assertFalse(filter.test(m4))
        
        filter.setValue('FoO')
        
        assertFalse(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
        assertFalse(filter.test(m4))
        
        filter.setCaseSensitive(false)
        
        assertTrue(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
        assertFalse(filter.test(m4))
        
        filter.setField(new AnnotationField('Test', 'description'))
        
        assertFalse(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
        assertFalse(filter.test(m4))
    }
}