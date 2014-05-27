package com.googlecode.lightest.core

class LightestContextTest extends GroovyTestCase {
    
    void testAddingArbitraryValues() {
        def context = new LightestContext()
        
        assertNull(context.foo)
        assertNull(context.bar)
        
        context.foo = 1
        context.bar = 2
        
        assertEquals(1, context.foo)
        assertEquals(2, context.bar)
        
        context.pushTaskProvider(null)
        context.foo = 3
        context.bar = 4
        
        assertEquals(3, context.foo)
        assertEquals(4, context.bar)
        
        context.push('foo', 5)
        context.push('bar', 6)
        
        assertEquals(5, context.foo)
        assertEquals(6, context.bar)
        
        context.push('foo', 7)
        context.push('bar', 8)
        
        assertEquals(7, context.foo)
        assertEquals(8, context.bar)
        
        context.foo = 9
        context.bar = 10
        
        // still reading from push()'d value
        assertEquals(7, context.foo)
        assertEquals(8, context.bar)
        
        context.popTaskProvider()
        
        // back to normal
        assertEquals(9, context.foo)
        assertEquals(10, context.bar)
        
        context.foo = null
        context.bar = null
        
        assertNull(context.foo)
        assertNull(context.bar)
    }
}
