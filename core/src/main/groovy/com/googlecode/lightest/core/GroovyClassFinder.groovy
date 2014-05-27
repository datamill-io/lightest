package com.googlecode.lightest.core

import org.testng.xml.XmlPackage

/**
 * Capable of finding Groovy classes by package.*/
class GroovyClassFinder {
    /** the class loader to use when finding classes */
    ClassLoader classLoader

    /** the classpaths to which to restrict the search */
    private List<String> testClassPaths

    GroovyClassFinder() {
        this(null, "")
    }

    GroovyClassFinder(ClassLoader classLoader, String testClassPath) {
        this.classLoader = classLoader
        setTestClasspath(testClassPath)
    }

    /**
     * Wraps the 3-argument form of findByPackage().*/
    List<String> findByPackage(XmlPackage xmlPackage) {
        return findByPackage(xmlPackage.getName(), xmlPackage.getInclude(),
                             xmlPackage.getExclude())
    }

    /**
     * Looks for Groovy classes using the currently set classpath that live in
     * the specified package or package hierarchy, and returns their names in a
     * List. Specific classes can be included or excluded. If a testClassPath
     * has been set on this object, only that classes that are found on that
     * class path are considered. If the class loader is not set on this
     * instance before this method is called, the current Thread's context
     * class loader will be used to load resources. The logic here mimics that
     * of org.testng.internal.PackageUtils.findClassesInPackage() .
     *
     * @param packageName
     * @param included
     * @param excluded
     */
    List<String> findByPackage(String packageName, List<String> included,
                               List<String> excluded) {
        def packageOnly = packageName
        def recursive = false

        if (packageName.endsWith('.*')) {
            def lastIndex = packageName.lastIndexOf('.*')
            packageOnly = packageName.substring(0, lastIndex)
            recursive = true
        }

        def classes = []
        def packageDirName = packageOnly.replace('.', '/')
        def dirs = (classLoader ?: Thread.currentThread().contextClassLoader)
                .getResources(packageDirName)

        while (dirs.hasMoreElements()) {
            def url = dirs.nextElement()

            if (!matchesTestClasspath(url, packageDirName, recursive)) {
                continue
            }

            def protocol = url.getProtocol()

            if ('file'.equals(protocol)) {
                findClassesInDirPackage(packageOnly, included, excluded,
                                        URLDecoder.decode(url.getFile(), 'UTF-8'), recursive,
                                        classes)
            }
            // other cases such as "jar" currently omitted
        }

        return classes
    }

    void setTestClasspath(String testClassPath) {
        testClassPaths = []

        if (!testClassPath) {
            return
        }

        def paths = testClasspath.split(File.pathSeparator)

        for (path in paths) {
            def lcPath = path.toLowerCase()

            if (lcPath.endsWith(".jar") || lcPath.endsWith(".zip")) {
                path = "${path}!/"
            } else {
                if (!path.endsWith(File.separator)) {
                    path = "${path}/"
                }
            }

            testClassPaths << path.replace('\\', '/')
        }
    }

    /**
     * Whether a given URL for a file in a package matches or "is covered by"
     * one of the classpaths set as the test classpath. If no test classpath
     * was specified, always returns true. 
     *
     * @param url the URL of the resource to test
     * @param lastFragment the package name to consider as an offset from
     *                      each test classpath
     * @param recursive whether resources contained in subpackages are
     *                      considered matches 
     */
    boolean matchesTestClasspath(URL url, String lastFragment, boolean recursive) {
        if (testClassPaths.size() == 0) {
            return true
        }

        def fileName = URLDecoder.decode(url.getFile(), 'UTF-8')

        for (path in testClassPaths) {
            def packagePath = path + lastFragment
            def i = fileName.indexOf(packagePath)

            if (i == -1 || (i > 0 && fileName.charAt(idx - 1) != '/')) {
                continue
            }

            if (fileName.endsWith(packagePath) || (recursive && fileName.charAt(i + packagePath.length()) == '/')) {
                return true
            }
        }

        return false
    }

    protected void findClassesInDirPackage(String packageName,
                                           List<String> included, List<String> excluded, String packagePath,
                                           final boolean recursive, List<String> classes) {
        def dir = new File(packagePath)

        //println "Looking for test classes in the directory: ${dir}"

        dir.eachFile { file ->
            if (file.isDirectory()) {
                if (recursive) {
                    findClassesInDirPackage(packageName + "." + file.getName(),
                                            included, excluded, file.getAbsolutePath(), recursive,
                                            classes)
                }
            } else {
                if (file.name.endsWith('.groovy')) {
                    def className = file.name.substring(0,
                                                        file.name.lastIndexOf('.'))

                    if (isIncluded(className, included, excluded)) {
                        //println "Including class: ${className}"
                        classes << "${packageName}.${className}"
                    } else {
                        //println "Excluding class: ${className}"
                    }
                }
            }
        }
    }

    protected boolean isIncluded(String name, List<String> included,
                                 List<String> excluded) {
        // If no includes nor excludes were specified, return true.
        if (included.size() == 0 && excluded.size() == 0) {
            return true
        }

        def isIncluded = included.any { (name =~ it).matches() }
        def isExcluded = excluded.any { (name =~ it).matches() }

        // exclusion takes precedence
        if (isExcluded) {
            return false
        }
        // no explicit inclusions means everything is included
        if (isIncluded || included.size() == 0) {
            return true
        }

        // includes were specified, and name was not explicitly included
        return false
    }

    static void main(args) {
        assert args.size() > 0

        def packageName = args[0]
        def included = []
        def excluded = []
        def finder = new GroovyClassFinder()

        if (args.size() > 1) {
            included << args[1]
        }
        if (args.size() > 2) {
            excluded << args[2]
        }

        println finder.findByPackage(packageName, included, excluded)
    }
}
