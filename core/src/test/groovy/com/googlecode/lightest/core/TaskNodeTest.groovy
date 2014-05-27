package com.googlecode.lightest.core

class TaskNodeTest extends GroovyTestCase {
    
    void testNodeValueIsSetProperly() {
        def n1 = new TaskNode(null, 'n1', 'v1')
        def n2 = new TaskNode(n1, 'n2', 'v2')
        def n3 = new TaskNode(n1, 'n3')
        def n4 = new TaskNode(n3, 'n4', 'v4')
        
        assertEquals(2, n1.children().size())
        assertFalse(n1.children().contains('v1'))
        assertTrue(n1.value() instanceof List)
        assertEquals(3, n1.value().size())
        assertTrue(n1.value().contains('v1'))
        assertEquals('v1', n1.nodeValue())
        
        assertEquals(0, n2.children().size())
        assertEquals('v2', n2.value())
        assertTrue(n2.value().contains('v2'))
        assertEquals('v2', n2.nodeValue())
        
        assertEquals(1, n3.children().size())
        assertTrue(n3.value() instanceof List)
        assertEquals(1, n3.value().size())
        assertTrue(n3.value().contains(n4))
        assertNull(n3.nodeValue())

        def n5 = new TaskNode(null, 'n5', [])

        assertNull(n5.nodeValue())
    }
    
    void testSettingValueAlsoSetsNodeValue() {
        def n = new TaskNode(null, 'n1', 'v1')
        
        assertEquals('v1', n.nodeValue())
        
        n.setValue('v2')
        
        assertEquals('v2', n.nodeValue())
        
        n.setValue([ 'v3', 'v4' ])
        
        // node value does not get set to non-List values
        assertEquals('v2', n.nodeValue())
    }
}