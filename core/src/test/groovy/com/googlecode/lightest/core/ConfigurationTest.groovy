package com.googlecode.lightest.core

class ConfigurationTest extends GroovyTestCase {
    def config
    
    @Override
    void setUp() {
        super.setUp()
        config = new Configuration()
    }
    
    void testParseConfigWithMissingBindingThrowsException() {
        def configText = 'config { classPaths { path ("${foo}") } }'
        
        shouldFail(MissingPropertyException.class) {
            config.parseConfig(configText)
        }
    }
    
    void testParseConfigWithBindingSpecified() {
        def binding = new Binding([ foo: 'bar' ])
        def shell = new GroovyShell(binding)
        def configText = 'config { classPaths { path ("${foo}") } }'
        
        config = new Configuration(shell)
        
        def node = config.parseConfig(configText)
        
        assertEquals('bar', node?.classPaths?.path[0].value())
    }
    
    void testConfigurationCanAccessBoundValuesViaBinding() {
        def configText = '''
config {
    classPaths {
        path ("${binding.variables.foo ?: 'baz'}")
    }
}
'''
        def tests = [
             [ [:], 'baz' ],
             [ [ foo: 'bar'], 'bar' ]
        ]

        for (test in tests) {
            config = new Configuration(test[0])
            def node = config.parseConfig(configText)
            assertEquals(test[1], node.classPaths.path[0].value())
        }
    }
    
    void testSetClassPaths() {
        def configText =
'''
config {
    classPaths {
        path ('foo')
        path ('bar')
    }
}
'''

        config.init(configText)
        
        def classPaths = config.getClassPaths()
        
        assertEquals(3, classPaths.size())
        assertEquals('foo', classPaths[0])
        assertEquals('bar', classPaths[1])
        assertEquals('.', classPaths[2])
    }
    
    void testDefaultOutputDir() {
        def outputDir = new File('lightest-report').getCanonicalPath()
        
        config.init("")
        assertEquals(outputDir, config.getOutputDir())
    }
    
    void testSetOutputDir() {
        def outputDir = new File('foo/bar').getCanonicalPath()
        def configText =
"""
config {
    outputDir ('${LightestUtils.backslash(outputDir)}')
}
"""
    
        config.init(configText)
        
        assertEquals(outputDir, config.getOutputDir())
    }

    void testSetPreferences() {
        def configText =
'''
config {
    prefs (class: 'com.googlecode.lightest.core.DummyPreferences') {
        foo ('deluxe')
        bar (0)
    }
}
'''
        config.init(configText)
        
        def prefs = config.getPreferences()
        
        assertEquals('com.googlecode.lightest.core.DummyPreferences',
            prefs.class.name)
        assertEquals('deluxe', prefs.getFoo())
        assertEquals(0, prefs.getBar())
    }
    
    void testDefaultEnvironments() {
        config.init("")
        
        def envs = config.getEnvironments()
        
        assertEquals(3, envs.size())
        assertEquals('unspecified1', envs[0].getId())
        assertEquals('unspecified2', envs[1].getId())
        assertEquals('unspecified3', envs[2].getId())
    }
    
    void testSetEnvironment() {
        def envClass =
            'com.googlecode.lightest.core.tutorial.TutorialEnvironment'
        def configText =
"""
config {
    envs (class: '${envClass}') {
        env (id: 'e') {
            world ('ooh')
        }
        env (id: 'mc^2') {
            world ('aah')
        }
    }
}
"""
        config.init(configText)
        
        def envs = config.getEnvironments()
        
        assertEquals(2, envs.size())
        assertEquals(envClass, envs[0].class.name)
        assertEquals('e', envs[0].getId())
        assertEquals('ooh', envs[0].getWorld())
        assertEquals(envClass, envs[1].class.name)
        assertEquals('mc^2', envs[1].getId())
        assertEquals('aah', envs[1].getWorld())
    }
    
    void testSetContextClass() {
        def contextClass = 'MyContext'
        def configText =
"""
config {
    context (class: '${contextClass}')
}
"""
        config.init(configText)
        
        assertEquals(contextClass, config.getContextClass())
    }
    
    void testDefaultReporter() {
        config.init("")
        
        def reporters = config.getReporters()
        
        assertEquals(2, reporters.size())
        assertEquals('com.googlecode.lightest.core.DefaultSummaryReporter',
            reporters[0].class.name)
        assertEquals('com.googlecode.lightest.core.DefaultDetailsReporter',
            reporters[1].class.name)
    }
    
    void testSetReporters() {
        def configText =
"""
config {
    reporters {
        reporter (class: 'com.googlecode.lightest.core.DummyReporter')
        reporter (class: 'com.googlecode.lightest.core.DummyReporter', role: '${Configuration.ROLE_XML_REPORTER}')
    }
}
"""
        
        config.init(configText)
        
        def reporters = config.getReporters()
        
        assertEquals(1, reporters.size())
        assertEquals('com.googlecode.lightest.core.DummyReporter',
            reporters[0].class.name)
        assertEquals('com.googlecode.lightest.core.DummyReporter',
            config.xmlReporter.class.name)
        assertEquals('com.googlecode.lightest.core.PendingReporter',
            config.pendingReporter.class.name)
        assertEquals('com.googlecode.lightest.core.FailedReporterDecorator',
            config.failedReporter.class.name)
        assertEquals('org.testng.reporters.FailedReporter',
            config.failedReporter.getDelegate().class.name)
    }
    
    void testSetListeners() {
        def configText =
'''
config {
    listeners {
        listener (class: 'com.googlecode.lightest.core.DummyListener') {
            someProperty ('someValue')
        }
    }
}
'''

        config.init(configText)
        
        def listeners = config.getListeners()
        
        assertEquals(1, listeners.size())
        assertEquals('com.googlecode.lightest.core.DummyListener',
            listeners[0].class.name)
        assertEquals('someValue', listeners[0].someProperty)
    }
    
    void testDefaultDispatcherAssignmentStrategy() {
        config.init("")
        
        def strategy = config.getDispatcherAssignmentStrategy()
        
        assertEquals('com.googlecode.lightest.core.SimpleDispatcherAssignmentStrategy',
            strategy.class.name)
        assertTrue(strategy instanceof QueuedDispatcherAssignmentStrategy)
    }
    
    void testSetDispatcherAssignmentStrategy() {
        def strategyClass = 'com.googlecode.lightest.core.DummyStrategy'
        def configText =
"""
config {
    dispatcherAssignmentStrategy (class: '${strategyClass}')
}
"""
        
        config.init(configText)
        
        def strategy = config.getDispatcherAssignmentStrategy()
        
        assertEquals(strategyClass, strategy.class.name)
    }
    
    void testCreateConfiguredInstance() {
        def instanceClassDefn =
'''
class SomeInstance {
    String simpleProp
    List<String> collectiveProps = []
}
'''
        def builder = new NodeBuilder()
        def instanceConfigNode = builder.instance (class: 'SomeInstance') {
            simpleProp     ('foo')
            collectiveProp ('bar')
            collectiveProp ('baz')
        }

        config.classLoader.parseClass(instanceClassDefn)

        def instance = config.createConfiguredInstance(instanceConfigNode)
        
        assertEquals('foo', instance.simpleProp)
        assertEquals(2, instance.collectiveProps.size())
        assertEquals('bar', instance.collectiveProps[0])
        assertEquals('baz', instance.collectiveProps[1])
    }
    
    void testCreateConfiguredReporterConfiguresWrappedDelegate() {
        def reporterClassDefn =
'''
import org.testng.IReporter
import org.testng.ISuite
import org.testng.xml.XmlSuite

class SomeReporter implements IReporter {
    String simpleProp

    void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
            String outputDirectory) {}
}
'''
        def builder = new NodeBuilder()
        def reporterConfigNode = builder.instance (class: 'SomeReporter') {
            simpleProp ('foo')
        }
        
        config.classLoader.parseClass(reporterClassDefn)
        
        def instance = config.createConfiguredReporter(reporterConfigNode)
        
        assertEquals(LightestReporterAdapter.class, instance.class)
        assertEquals('SomeReporter', instance.getDelegate().class.name)
        assertEquals('foo', instance.getDelegate().simpleProp)
    }
}