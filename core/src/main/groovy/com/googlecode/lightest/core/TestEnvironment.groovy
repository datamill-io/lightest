package com.googlecode.lightest.core

/**
 * A convenient base class for defining custom test environments. Subclasses
 * should only add simple property value definitions - i.e. Date, String, etc.*/
class TestEnvironment implements ITestEnvironment, Serializable {
    String id

    private Set<String> propertyOrder

    TestEnvironment() {
        this('unspecified')
    }

    TestEnvironment(String id) {
        this.id = id
        this.propertyOrder = new LinkedHashSet<String>()
    }

    Map<String, Object> settings() {
        def settings = this.properties

        settings.remove('class')
        settings.remove('metaClass')
        settings.remove('id')

        def orderedSettings = new LinkedHashMap<String, Object>()

        propertyOrder.each { name -> orderedSettings[name] = settings[name]
        }

        // known orderings have already been established, ok to repeat
        orderedSettings.putAll(settings)

        return orderedSettings
    }

    /**
     * Sets properties of the environment based on a Node configuration, whose
     * child Nodes contain name-value pairs, retrievable by name() and value(),
     * respectively. This is chiefly useful for later enumerating properties
     * initialized in this way in the same order they were specified, i.e.
     * by calling settings().
     *
     * @param config
     */
    void initializeProperties(Node config) {
        for (propertyConfig in config.children()) {
            propertyOrder << propertyConfig.name()
            this."${propertyConfig.name()}" = propertyConfig.value()
        }
    }
}