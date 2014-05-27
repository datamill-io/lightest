package com.googlecode.lightest.core

class LightestContextFactoryTest extends GroovyTestCase {
    def classLoader
    
    @Override
    void setUp() {
        def contextClassLoader = Thread.currentThread().contextClassLoader
        classLoader = new GroovyClassLoader(contextClassLoader)
    }
    
    void testSetDefaultContextClassOk() {
        def contextClass = LightestContextFactory.DEFAULT_CONTEXT_CLASS
        def factory = new LightestContextFactory(contextClass, classLoader,
            null)
        
        assertEquals(contextClass, factory.getContextClass())
        
        def context = factory.create()
        
        assertEquals(contextClass, context.class.name)
    }
    
    void testSetSubclassedContextClassOk() {
        // cache the subclass in the class loader
        classLoader.parseClass('''
            import com.googlecode.lightest.core.LightestContext
            
            class MyContext extends LightestContext {
                String foo
                int bar
                boolean baz
            }''')
        
        def factory = new LightestContextFactory('MyContext', classLoader, null)
        
        assertEquals('MyContext', factory.getContextClass())
        
        def context = factory.create()
        
        assertEquals('MyContext', context.class.name)
    }
    
    void testSetNonSubclassedContextClassFails() {
        def factory = new LightestContextFactory('java.lang.String',
            classLoader, null)
        
        try {
            def context = factory.create()
            fail('Expected an exception, but none was thrown!')
        }
        catch (Exception e) {
            def msg = ('Class java.lang.String is not a LightestContext, and '
                + 'cannot be used as a context class')
            assertEquals(msg, e.getMessage())
        }
    }
}
