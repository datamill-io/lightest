package com.googlecode.lightest.distributed

class TestSuiteHolderTest extends GroovyTestCase {
    void testLoadASuiteOfClasses() {
        TestSuiteHolder tsh = new TestSuiteHolder()
        tsh.configure(actualConfig)

        ArrayList<String> testNames =
                tsh.getTestNames(["src\\test\\resources\\com\\googlecode\\lightest\\distributed\\suite-class-ex.xml"])

        println("Test names: ${testNames}")
        assertEquals(2, testNames.size())
    }

    void testLoadASuiteOfPackages() {
        TestSuiteHolder tsh = new TestSuiteHolder()
        tsh.configure(actualConfig)

        ArrayList<String> testNames =
                tsh.getTestNames(["src\\test\\resources\\com\\googlecode\\lightest\\distributed\\suite-package-ex.xml"])

        println("Test names: ${testNames}")
        assertEquals(2, testNames.size())
    }

    def actualConfig = '''
def username = System.getProperty('user.name')
def thisDir = new File( "." ).absolutePath
def baseDir = "${thisDir}/core"
config {
    classPaths {
//        path ("${baseDir}/lib/log4j.jar")
//        path ("${baseDir}/lib/merlia.jar")
//        path ("${baseDir}/lib/edix12validation.jar")
//        path ("${baseDir}/lib/dtxpi.jar")
//        path ("${baseDir}/lib/commons-net.jar")
//        path ("${baseDir}/lib/dbconnection.jar")
//        path ("${baseDir}/lib/selenium-java.jar")
//        path ("${baseDir}/lib/guava.jar")
//        path ("${baseDir}/lib/j2eeclient.jar")
//        path ("${baseDir}/lib/logutilities.jar")
//        path ("${baseDir}/lib/pgenengine.jar")
//        path ("${baseDir}/lib/pgenpayloadprocessor.jar")
//        path ("${baseDir}/lib/ats-core.jar")
//        path ("${baseDir}/lib/lightest-ats.jar")
//        path ("${baseDir}/lib/jboss-jms-api.jar")
//        path ("${baseDir}/lib/jnp-client.jar")
//        path ("${baseDir}/lib/hornetq-jms-client.jar")
//        path ("${baseDir}/lib/netty.jar")
//        path ("${baseDir}/lib/hornetq-core-client.jar")
//        path ("${baseDir}/lib/hornetq-commons.jar")
//        path ("${baseDir}/lib/commandproc.jar")
//        path ("${baseDir}/conf")
        path ("${baseDir}/")
        path ("${baseDir}/src/test/resources/")
        path ("${baseDir}/src/test/groovy/")
    }
}'''
}
