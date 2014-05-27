package com.googlecode.lightest.core.filter

import org.testng.annotations.Test

class AnnotationEqualsFilterTest extends GroovyTestCase {
    
    @Test(description = 'foo')
    void annotatedMethod1() {
        // nothing doing
    }
    
    @Test(description = 'bar')
    void annotatedMethod2() {
        // nothing doing
    }
    
    @Test
    void annotatedMethod3() {
        // nothing doing
    }
    
    void testFilterTest() {
        def filter = new AnnotationEqualsFilter()
        def m1 = this.class.methods.find { it.name == 'annotatedMethod1' }
        def m2 = this.class.methods.find { it.name == 'annotatedMethod2' }
        def m3 = this.class.methods.find { it.name == 'annotatedMethod3' }
        
        assertNotNull(m1)
        assertNotNull(m2)
        assertNotNull(m3)
        
        filter.setField(new AnnotationField('Test', 'description'))
        filter.setValue('foo')
        filter.setCaseSensitive(true)
        
        assertTrue(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
        
        filter.setValue('FoO')
        
        assertFalse(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
        
        filter.setCaseSensitive(false)
        
        assertTrue(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
        
        filter.setField(new AnnotationField('Test', 'groups'))
        
        assertFalse(filter.test(m1))
        assertFalse(filter.test(m2))
        assertFalse(filter.test(m3))
    }
}