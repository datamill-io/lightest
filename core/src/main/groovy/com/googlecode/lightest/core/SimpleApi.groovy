/**
 * A simple IDomainSpecificApi implementation that creates tasks from the
 * package named by the parameter passed to the constructor, or added via
 * addPackage(). The class loader used to load the task classes is passed in as
 * the first argument to all constructors. If the object is passed into the
 * constructor is not a class loader, the class loader for that object's class
 * will be used.*/
package com.googlecode.lightest.core

import com.googlecode.lightest.core.IDomainSpecificApi
import com.googlecode.lightest.core.ITask

class SimpleApi implements IDomainSpecificApi {
    private classLoader
    private Set<String> packages
    private Map<String, Class> classCache;

    SimpleApi() {
        this(null, "")
    }

    SimpleApi(ClassLoader classLoader) {
        this(classLoader, "")
    }

    SimpleApi(String packageName) {
        this(null, packageName)
    }

    SimpleApi(ClassLoader classLoader, String packageName) {
        this.classLoader = classLoader
        this.packages = new HashSet<String>()
        this.packages << packageName ?: ""
        classCache = new HashMap<String, Class>();
    }

    /**
     * Adds another package whose tasks to include in the API.*/
    void addPackage(String packageName) {
        packages << packageName
    }

    /**
     * Creates and returns a task as a class in the specified package.
     *
     * @param name the name of the task, which is also the name of its
     *              corresponding class
     */
    // TODO - add detection of fully specified task names
    ITask getTask(String name) {
        Class taskClass = classCache.get(name);

        if (!taskClass) {
            taskClass = huntForTasks(name, taskClass)
            if (taskClass) {
                classCache.put(name, taskClass)
            } else {
                return null
            }
        }

        def task = taskClass.newInstance()
        if (task instanceof ITask) {
            return task
        }


        return null
    }

    private Class<?> huntForTasks(String name, Class taskClass) {
        synchronized (TestRunner.getLockObject()) {
            def taskLoader = (classLoader ?: Thread.currentThread().getContextClassLoader())

            for (packageName in packages) {
                def qname = packageName ? "${packageName}.${name}" : name

                try {
                    taskClass = taskLoader.loadClass(qname)
                    return taskClass
                } catch (ClassNotFoundException cnfe) {
                    continue
                }
            }
        }
        return null
    }
}