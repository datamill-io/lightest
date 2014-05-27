package com.googlecode.lightest.core

class LightestUtilsTest extends GroovyTestCase {
    
    void testGetSettableProperties() {
        def gcl = new GroovyClassLoader()
        def classDefn = '''
            class Foo {
                int a
                String b
                boolean c
                
                private float d
                
                void setE() {}
                void setFlatScreenTv() {}
                Object getG() {}
            }
        '''
        
        def clazz = gcl.parseClass(classDefn)
        def props = LightestUtils.getSettableProperties(clazz)
        
        assertTrue(props.contains('a'))
        assertTrue(props.contains('b'))
        assertTrue(props.contains('c'))
        assertTrue(props.contains('e'))
        assertTrue(props.contains('flatScreenTv'))
        assertEquals(5, props.size())
    }
}
