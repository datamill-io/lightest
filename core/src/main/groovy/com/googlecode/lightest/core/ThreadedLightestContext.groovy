package com.googlecode.lightest.core

class ThreadedLightestContext extends InheritableThreadLocal<LightestContext> {
    private LightestContextFactory factory

    /**
     * @param factory the factory that will be used to create new context
     *                 instances on each thread
     */
    ThreadedLightestContext(LightestContextFactory factory) {
        this.factory = factory
    }

    @Override
    protected LightestContext initialValue() {
        return factory.create()
    }

    @Override
    protected LightestContext childValue(LightestContext parentValue) {
        return factory.create(parentValue)
    }
}
