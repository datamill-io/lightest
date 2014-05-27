package com.googlecode.lightest.core

import groovy.xml.MarkupBuilder
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StackTraceUtils
import org.codehaus.groovy.runtime.StringBufferWriter
import org.testng.ITestContext
import org.testng.ITestResult
import org.testng.xml.XmlSuite

public class LightestUtils {

    /**
     * Return the String representation of a stack trace with extraneous Groovy
     * MOP entries removed.
     *
     * @param t
     */
    static String getSanitizedStackTrace(Throwable t) {
        def sb = new StringBuffer()
        def writer = new StringBufferWriter(sb)

        StackTraceUtils.deepSanitize(t)
        t.printStackTrace(new PrintWriter(writer))

        return sb.toString()
    }

    /**
     * Returns the entry in the stack trace of the current thread that
     * represents the most recent method call in a subclass of
     * LightestTestCase. If none is found, returns null.*/
    static StackTraceElement getTestCaseStackElement() {
        def thread = Thread.currentThread()
        def clazz = null

        for (entry in thread.getStackTrace()) {
            try {
                synchronized (TestRunner.getLockObject()) {
                    clazz = Class.forName(entry.getClassName(), true,
                                          thread.getContextClassLoader())
                }
                if (LightestTestCase.class != clazz && LightestTestCase.class.isAssignableFrom(clazz)) {
                    return entry
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return null
    }

    /**
     * Returns the entry in the stack trace of the current thread that
     * represents the test script execution point line number.
     * If none is found, returns message.*/
    static String getTestCaseStackElementLineNumber() {
        def thread = Thread.currentThread()

        for (entry in thread.getStackTrace()) {
            try {
                if ("tests".equals(entry.toString().substring(0, 5))) {
                    return entry.getLineNumber().toString()
                }
            } catch (Exception e) {
                return "Problem in LightestUtils.groovy LineNumber method."
            }
        }
        return "Oops! Problem finding line number."
    }

    /**
     * Sets this specified node builder instance as the delegate for any
     * Closures in the argument list.
     *
     * @param args a list of arguments as would be passed into
     *                 methodMissing() or invokeMethod().
     * @param builder the builder to delegate to
     *
     */
    static void setBuilderAsDelegate(args, BuilderSupport builder) {
        def argsList = InvokerHelper.asList(args)

        argsList.each {
            if (it instanceof Closure) {
                it.setDelegate(builder)
                it.setResolveStrategy(Closure.DELEGATE_FIRST)
            }
        }
    }

    /**
     * Creates a Lightest suite file for a given set of test classes in a
     * temporary location, and returns the file.
     *
     * @param testClasses the LightestTestCase classes
     * @param threadCount the number of parallel threads to use when executing
     *                     the test methods. If less than 2, the test methods
     *                     will not be specified to be run in parallel.
     */
    static File createSuiteFile(List<Class> testClasses, int threadCount = 1, boolean verbose = true) {
        def suiteFile = File.createTempFile('lightest-suite', '.xml')
        def testClassNames = testClasses.collect { it.name }
        def writer = new FileWriter(suiteFile)

        writeSuiteXML(testClassNames, threadCount, writer, verbose)

        return suiteFile
    }

    /**
     * Returns as a String the TestNG suite XML representing the List of String
     * class names.*/
    static String createSuiteXML(List<String> testClassNames, int threadCount, boolean verbose = true) {
        def writer = new StringWriter()

        writeSuiteXML(testClassNames, threadCount, writer, verbose)

        return writer.toString()
    }

    /**
     * Writes an XML representation of a Lightest suite file for a given
     * set of test classes, using the specified Writer.
     *
     * @param testClassNames the names of LightestTestCase classes
     * @param threadCount the number of parallel threads to use when
     *                        executing the test methods. If less than 2, the
     *                        test methods will not be specified to be run in
     *                        parallel.
     * @param writer the writer to use when writing the XML
     */
    static void writeSuiteXML(List<String> testClassNames, int threadCount,
                              Writer writer, boolean verbose) {
        def builder = new MarkupBuilder(writer)

        builder.suite(name: 'Suite1', parallel: threadCount > 1 ? XmlSuite.PARALLEL_METHODS : XmlSuite.PARALLEL_NONE,
                      'thread-count': threadCount, verbose: verbose ? 1: 0) {
            test(name: 'Test1') {
                classes {
                    for (testClassName in testClassNames) {
                        'class'(name: testClassName)
                    }
                }
            }
        }
    }

    /**
     * Writes an XML representation of a Lightest suite file for a given
     * set of test classes and methods, using the specified Writer.
     *
     * @param classesAndMethods - a map of method names keyed by the class name that should be re-run
     * @param threadCount - the thread count
     * @param writer - the writer
     * */
    static void writeSuiteXML(Map<String, Set<String>> classesAndMethods, int threadCount,
                              Writer writer) {
        def builder = new MarkupBuilder(writer)

        builder.suite(name: 'Suite1', parallel: threadCount > 1 ? XmlSuite.PARALLEL_METHODS : XmlSuite.PARALLEL_NONE,
                      'thread-count': threadCount, verbose: '1') {
            test(name: 'Test1') {
                classes {
                    for (testClassName in classesAndMethods.keySet()) {
                        'class'(name: testClassName) {
                            def testMethods = classesAndMethods.get(testClassName)
                            if (testMethods) {
                                methods {
                                    for (testMethodName in testMethods) {
                                        include(name: testMethodName)
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the names of all settable properties for a given class, as
     * defined by having "setX()" methods available for property "x". Certain
     * Groovy properties are excluded, such as "property" and "metaClass".
     *
     * @param clazz
     */
    static Collection<String> getSettableProperties(Class clazz) {
        def setters = clazz.methods.findAll { it.name.startsWith('set') }
        def settable = setters.collect {
            it.name[3].toLowerCase() + it.name.substring(4)
        }

        settable.remove('property')
        settable.remove('metaClass')

        return settable
    }

    /**
     * Adds all members of a list of class paths as URL's to the given class
     * loader. New URL's are added only if they do not already exist for the
     * class loader.
     *
     * @param paths the paths to add
     * @param classLoader the class loader to add to
     */
    static void addClassPaths(List<String> paths, URLClassLoader classLoader) {
        paths.each { path ->
            def url = new File(path).getCanonicalFile().toURL()
            synchronized (TestRunner.getLockObject()) {
                if (!classLoader.getURLs().find { it == url }) {
                    classLoader.addURL(url)
                }
            }
        }
    }

    /**
     * Returns the string with all occurrences of backslashes,
     * backslash-escaped.*/
    static String backslash(String s) {
        return s.replaceAll('\\\\', '\\\\\\\\')
    }
}