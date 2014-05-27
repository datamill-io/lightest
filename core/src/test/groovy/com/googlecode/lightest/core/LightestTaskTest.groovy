package com.googlecode.lightest.core

class LightestTaskTest extends GroovyTestCase {
    
    void testConfigurableProperties() {
        def gcl = new GroovyClassLoader()
        def classDefn = '''
            import com.googlecode.lightest.core.*
            
            class MyTask extends LightestTask {
                String foo
                
                int getBar() {}
                void setBar(int bar) {}
                
                void doPerform(ITaskResult result) {
                }
            }
        '''
        
        def clazz = gcl.parseClass(classDefn)
        def task = clazz.newInstance()
        def props = task.configurableProperties()
        
        assertTrue(props.contains('foo'))
        assertEquals(1, props.size())
    }
    
    void testConfigureCastsPropertyTypes() {
        def gcl = new GroovyClassLoader()
        def classDefn = '''
            import com.googlecode.lightest.core.*
            
            class MyTask extends LightestTask {
                String s
                int i1
                Integer i2
                boolean b1
                Boolean b2
                
                void doPerform(ITaskResult result) {
                }
            }
        '''
        
        def clazz = gcl.parseClass(classDefn)
        def task = clazz.newInstance()
        def tests = [
            [ s: '2020', i1: 1942, i2: 1024, b1: true, b2: false ],
            [ s: 2020, i1: '1942', i2: '1024', b1: 'true', b2: 'false' ]
        ]
        
        for (attrs in tests) {
            def config = new TaskNode(null, 'n1', attrs, 'v1')
            
            task.configure(config, null)
            
            assertEquals('2020', task.s)
            assertEquals(1942, task.i1)
            assertEquals(1024, task.i2)
            assertEquals(true, task.b1)
            assertEquals(false, task.b2)
        }
    }
    
    void testGetParamsSortsByParameterName() {
        def task = [ doPerform: { r -> } ] as LightestTask
        def map = new TreeMap()
        def attributes = [:]
        
        for (letter in 'z'..'a') {
            map[letter] = letter
            attributes[letter] = letter
        }
        
        task.config = new TaskNode(null, 'n1', attributes, 'v1')
        
        assertEquals(map, task.getParams())
    }
    
}