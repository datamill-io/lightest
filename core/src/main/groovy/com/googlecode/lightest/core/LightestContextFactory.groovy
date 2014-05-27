package com.googlecode.lightest.core

/**
 * A factory class for LightestContext instances.*/
class LightestContextFactory {
    public static final String DEFAULT_CONTEXT_CLASS =
            'com.googlecode.lightest.core.LightestContext'

    String contextClass
    ClassLoader classLoader

    /**
     * the directory in which the report will be generated. This value will be
     * passed on to all contexts created by this factory.*/
    String outputDir

    /**
     * @param contextClass the binary name of the context class
     * @param classLoader the class loader to use to load the context class.
     *                      May be null.
     * @param outputDir the output directory value to make available on all
     *                      context instances created by this factory.
     */
    LightestContextFactory(String contextClass, ClassLoader classLoader,
                           String outputDir) {
        this.contextClass = contextClass
        this.classLoader = classLoader
        this.outputDir = outputDir
    }

    /**
     * Creates and returns a new LightestContext instance, based on the class
     * set as the context class on this object. The class must extend
     * LightestContext; an exception will be thrown if it does not. This method
     * will throw a ClassNotFoundException if the class can't be loaded using
     * the sepecified class loader, or the current Thread's context class
     * loader if no class loader was specified.*/
    LightestContext create() {
        assert contextClass != null

        def clazz
        synchronized (TestRunner.getLockObject()) {
            clazz = Class.forName(contextClass, true, (classLoader ?: Thread.currentThread().contextClassLoader))
        }

        if (!LightestContext.isAssignableFrom(clazz)) {
            throw new Exception(
                    "Class ${contextClass} is not a " + 'LightestContext, and cannot be used as a context class')
        }

        def context = clazz.newInstance()

        context.outputDir = outputDir

        return context
    }

    /**
     * Creates and returns a new LightestContext instance, with a parent
     * instance to model from.
     *
     * @param parentContext
     */
    LightestContext create(LightestContext parentContext) {
        def context = create()
        context.inheritContextFromParent(parentContext)
        return context
    }
}
