package com.googlecode.lightest.core

import org.codehaus.groovy.control.ConfigurationException
import org.codehaus.groovy.runtime.StackTraceUtils

import org.testng.IReporter
import org.testng.ITestNGListener
import org.testng.reporters.FailedReporter

/**
 * Represents a configuration of the test run*/
// TODO - separate Configuration value object from a ConfigurationFactory
class Configuration implements IConfiguration {
    public static final IPreferences UNSPECIFIED_PREFS = [:] as IPreferences
    public static final List<ITestEnvironment> UNSPECIFIED_ENVS = [new TestEnvironment('unspecified1'),
                                                                   new TestEnvironment('unspecified2'),
                                                                   new TestEnvironment('unspecified3')]

    public static final String ROLE_XML_REPORTER = 'XMLReporter'
    public static final String ROLE_PENDING_REPORTER = 'PendingReporter'
    public static final String ROLE_FAILED_REPORTER = 'FailedReporter'

    public static final String DEFAULT_ASSIGNMENT_STRATEGY =
            'com.googlecode.lightest.core.SimpleDispatcherAssignmentStrategy'

    private GroovyShell shell
    private GroovyClassLoader classLoader
    private classPaths
    private outputDir
    private prefs
    private envs
    private List<ILightestReporter> reporters
    private ILightestReporter xmlReporter
    private ILightestReporter pendingReporter
    private ILightestReporter failedReporter
    private List<ITestNGListener> testNGListeners
    private List<IRunValidator> runValidators = []
    private String contextClass
    private IDispatcherAssignmentStrategy assignmentStrategy
    private ITaskDispatchStrategy dispatchStrategy

    Configuration() {
        this(new GroovyShell())
    }

    Configuration(GroovyShell shell) {
        this(shell, new GroovyClassLoader())
    }

    /**
     * A convenience constructor for creating a Configuration object whose
     * text is parsed with the given variables in scope.
     *
     * @param vars a Map of String's to Object's that will be used to
     *              initialize a Binding object, and set on the GroovyShell
     *              used to evaluate the configuration text.
     */
    Configuration(Map<String, Object> vars) {
        this(new GroovyShell(new Binding(vars)))
    }

    /**
     * @param shell the GroovyShell to be used to evaluate the
     *                     configuration text
     * @param classLoader the class loader to use to load classes referenced
     *                     in the configuration text
     */
    Configuration(GroovyShell shell, GroovyClassLoader classLoader) {
        this.shell = shell
        this.classLoader = classLoader

        xmlReporter = new LightestReporterAdapter(new XMLReporter())
        pendingReporter = new PendingReporter()
        failedReporter = new FailedReporterDecorator(new FailedReporter())

        xmlReporter.setUpdateEnabled(true)
        xmlReporter.setScheduled(true)

        failedReporter.setUpdateEnabled(true)
        failedReporter.setScheduled(true)
    }

    /**
     * Initializes the configuration.
     *
     * @param configText a text representation of the configuration to
     *                    initialize. See the Lightest documentation for the
     *                    format of this text.
     */
    void init(String configText) {
        Node config = parseConfig(configText)

        setClassPaths(config)
        setOutputDir(config)
        setPreferences(config)
        setEnvironments(config)
        setReporters(config)
        setListeners(config)
        setRunValidators(config)
        setContextClass(config)
        setDispatcherAssignmentStrategy(config)
        setTaskDispatchStrategy(config)
    }

    protected Node parseConfig(String configText) {
        if (configText =~ /\S/) {
            def header = 'def config = new NodeBuilder()\n'

            try {
                return shell.evaluate(header + configText)
            } catch (e) {
                print 'Caught exception parsing the following configuration '
                println "(binding: ${shell.getContext().getVariables()}):"
                println configText
                StackTraceUtils.sanitize(e)
                throw e
            }
        }

        return new Node(null, 'config')
    }

    /**
     * Determines if the specified configuration is a valid one. If not, throws
     * an exception. Not implemented.*/
    void validate() {
        // TODO - implement
    }

    private void setClassPaths(Node config) {
        classPaths = []

        if (config.classPaths.size() > 0) {
            config.classPaths[0].path.each {
                classPaths << it.value()
            }
        }

        // the current directory should be added last
        classPaths << '.'
    }

    private void setOutputDir(Node config) {
        outputDir = config.outputDir[0]?.value() ?: 'lightest-report'
        outputDir = new File(outputDir).getCanonicalPath()
    }

    /**
     * Initializes the preferences instance according to the configuration. If
     * the configuration references a property that is not a property of the
     * preference, an exception is thrown.
     *
     * @param config the configuration containing the preference information.
     *                It must have a "prefs" child with a "class" attribute.
     */
    private void setPreferences(Node config) {
        if (config.prefs.size() == 0) {
            prefs = UNSPECIFIED_PREFS
            return
        }

        prefs = createConfiguredInstance(config.prefs[0])
    }

    private void setEnvironments(Node config) {
        if (config.envs.size() == 0 || config.envs.env.size() == 0) {
            envs = UNSPECIFIED_ENVS
            return
        }

        assert config.envs[0].'@class' != null

        envs = []

        // TODO - is there any reason why all environments have to be of the
        //        same class?
        def envClass = loadClass(config.envs.'@class')

        for (envConfig in config.envs[0].env) {
            validateEnvironment(envConfig, envs)

            def env = envClass.newInstance()

            env.setId(envConfig.'@id')
            env.initializeProperties(envConfig)

            envs << env
        }
    }

    // TODO - replace validation with groovytools-builder metabuilder schema
    private void validateEnvironment(envConfig, envs) {
        def id = envConfig.'@id'

        if (!id) {
            def msg = ("The 'id' attribute must be specified for the declared " + "environment ${envConfig}")
            throw new ConfigurationException(msg)
        } else if (envs.find { it.getId() == id }) {
            def msg = "The id '${id}' is not unique among declared environments"
            throw new ConfigurationException(msg)
        }
    }

    private void setContextClass(Node config) {
        contextClass = (config.context[0]?.'@class' ?: LightestContextFactory.DEFAULT_CONTEXT_CLASS)
    }

    private void setReporters(Node config) {
        if (config.reporters.size() == 0) {
            reporters = ['com.googlecode.lightest.core.DefaultSummaryReporter',
                         'com.googlecode.lightest.core.DefaultDetailsReporter'].collect { loadClass(it).newInstance() }
        } else {
            reporters = []

            config.reporters[0].reporter.each {
                switch (it.'@role') {
                    case ROLE_XML_REPORTER:
                        xmlReporter = (createConfiguredReporter(it) ?: xmlReporter)
                        break

                    case ROLE_PENDING_REPORTER:
                        pendingReporter = (createConfiguredReporter(it) ?: pendingReporter)
                        break

                    case ROLE_FAILED_REPORTER:
                        failedReporter = (createConfiguredReporter(it, true) ?: failedReporter)
                        break

                    default:
                        reporters << createConfiguredReporter(it)
                        break
                }
            }
        }
    }

    private void setListeners(Node config) {
        if (config.listeners.size() > 0) {
            testNGListeners = []

            config.listeners[0].listener.each {
                testNGListeners << createConfiguredInstance(it)
            }
        }
    }

    private void setRunValidators(Node config) {
        runValidators = []
        if (config.runValidators.size() > 0) {
            config.runValidators[0].runValidator.each {
                runValidators << createConfiguredInstance(it)
            }
        }
    }

    private void setDispatcherAssignmentStrategy(Node config) {
        assignmentStrategy = ((config.dispatcherAssignmentStrategy.'@class') ? createConfiguredInstance(
                config.dispatcherAssignmentStrategy[0]) : loadClass(DEFAULT_ASSIGNMENT_STRATEGY).newInstance())
    }

    private void setTaskDispatchStrategy(Node config) {
        if (config.taskDispatchStrategy.size() == 0) {
            return
        }

        dispatchStrategy = createConfiguredInstance(config.taskDispatchStrategy[0])
    }

    /**
     * Loads the class using a GroovyClassLoader whose classpath is set to
     * include all paths that have been parsed from the configuration text.
     *
     * @param className
     */
    private Class loadClass(String className) {
        synchronized (TestRunner.getLockObject()) {
            LightestUtils.addClassPaths(classPaths, classLoader)
            return classLoader.loadClass(className, true)
        }
    }

    /**
     * Returns a new instance of a class, as defined in a configuration node.
     * The class' binary name must be specified in the "class" attribute, and
     * the node's children are considered name-value pairs that can be used
     * to set properties on the new instance. The class loader used to load
     * the class will include at least all paths returned by getClassPaths().
     *
     * @param instanceConfig
     */
    protected Object createConfiguredInstance(Node instanceConfig) {
        assert instanceConfig.'@class' != null

        def instance = loadClass(instanceConfig.'@class').newInstance()

        configureInstance(instance, instanceConfig)

        return instance
    }

    protected void configureInstance(Object instance, Node instanceConfig) {
        instanceConfig.children().each {
            def propName = it.name()
            def metaClass = instance.class.metaClass

            if (metaClass.getMetaProperty(propName)) {
                instance."${propName}" = it.value()
                return
            }

            def pluralPropName = "${propName}s"

            if (metaClass.getMetaProperty(pluralPropName)) {
                def prop = instance."${pluralPropName}"
                if (prop != null && prop instanceof Collection) {
                    instance."${pluralPropName}" << it.value()
                    return
                }
            }
        }
    }

    /**
     * Returns a configured ILightestReporter. If the new'd instance is not
     * an ILightestReporter, but is an IReporter, it is wrapped with the
     * appropriate decorator class - the FailedReporterDecorator if
     * isFailedReporter is true, and LightestReporterAdapter otherwise. If the
     * instance is neither an ILightestReporter nor a IReporter, returns null.*/
    protected ILightestReporter createConfiguredReporter(Node instanceConfig,
                                                         boolean isFailedReporter = false) {
        assert instanceConfig.'@class' != null

        def reporter = loadClass(instanceConfig.'@class').newInstance()

        if (reporter instanceof ILightestReporter) {
            configureInstance(reporter, instanceConfig)
        } else if (reporter instanceof IReporter) {
            if (isFailedReporter) {
                reporter = new FailedReporterDecorator(reporter)
            } else {
                reporter = new LightestReporterAdapter(reporter)
            }

            configureInstance(reporter, instanceConfig)
            configureInstance(reporter.getDelegate(), instanceConfig)
        } else {
            reporter = null
        }

        return reporter
    }

    /**
     * Returns the class loader used to load the configuration-related classes.*/
    GroovyClassLoader getClassLoader() {
        return classLoader
    }

    /**
     * Returns a List of Strings representing class paths under which tasks
     * and potentially environment and preference classes are defined. The
     * current directory is added by default.*/
    List<String> getClassPaths() {
        return classPaths
    }

    /**
     * Returns the output directory for the test run report. The default output
     * directory is "lightest-report".*/
    String getOutputDir() {
        return outputDir
    }

    /**
     * Returns an instance of the concrete implementation of IPreferences.*/
    IPreferences getPreferences() {
        return prefs
    }

    /**
     * Returns a List of ITestEnvironment instances. The UNSPECIFIED_ENVS list
     * containing three default environment entries will be returned if
     * unspecified.*/
    List<ITestEnvironment> getEnvironments() {
        return envs
    }

    String getContextClass() {
        return contextClass
    }

    List<ILightestReporter> getReporters() {
        return reporters
    }

    ILightestReporter getXMLReporter() {
        return xmlReporter
    }

    ILightestReporter getPendingReporter() {
        return pendingReporter
    }

    ILightestReporter getFailedReporter() {
        return failedReporter
    }

    List<ITestNGListener> getListeners() {
        return testNGListeners ?: []
    }

    List<IRunValidator> getRunValidators() {
        return runValidators
    }

    /**
     * Returns the specified dispatcher assignment strategy. A DEFAULT_STRATEGY
     * will be returned if unspecified.*/
    IDispatcherAssignmentStrategy getDispatcherAssignmentStrategy() {
        return assignmentStrategy
    }

    ITaskDispatchStrategy getTaskDispatchStrategy() {
        return dispatchStrategy
    }
}
