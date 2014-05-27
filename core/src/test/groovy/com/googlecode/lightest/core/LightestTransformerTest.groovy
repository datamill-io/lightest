package com.googlecode.lightest.core

class LightestTransformerTest extends GroovyTestCase {
    def outputDir
    def transformer
    
    @Override
    void setUp() {
        def outputPath = System.getProperty('java.io.tmpdir')
        
        outputDir = new File(outputPath)
        transformer = new LightestTransformer()
        transformer.setParameter('testNgXslt.outputDir', outputPath)
    }

    /**
     * Returns a GPathResult representing the test results in the HTML base
     * report for a given suite, by name. Two sets of results will be included:
     * one groups results by status, and the other groups results by class.
     * 
     * @param suiteName  a HTML report for a suite with this name is expected
     * 					 to exist in outputDir
     */
    protected getMethodRows(String suiteName) {
        def reportFile = new File(outputDir, "${suiteName}.html")
        def root = new XmlSlurper().parse(reportFile)
        def rows = []
        
        for (node in root.depthFirst()) {
            if (node.'@class' == 'testMethodStatusPASS') {
                rows << node
            }
        }
        
        return rows
    }
    
    void testTransformPreservesOrderingOfContemporaneousResults() {
        def s =
'''
<testng-results>
  <suite name="Suite1">
    <test name="Test1">
      <class name="Tutorial">
        <test-method status="PASS" signature="a()" name="a" duration-ms="72" started-at="2008-11-30T18:39:49Z" finished-at="2008-11-30T18:39:49Z"></test-method>
        <test-method status="PASS" signature="b()" name="b" duration-ms="72" started-at="2008-11-30T18:39:49Z" finished-at="2008-11-30T18:39:49Z"></test-method>
        <test-method status="PASS" signature="c()" name="c" duration-ms="72" started-at="2008-11-30T18:39:49Z" finished-at="2008-11-30T18:39:49Z"></test-method>
        <test-method status="PASS" signature="d()" name="d" duration-ms="72" started-at="2008-11-30T18:39:49Z" finished-at="2008-11-30T18:39:49Z"></test-method>
        <test-method status="PASS" signature="e()" name="e" duration-ms="72" started-at="2008-11-30T18:39:49Z" finished-at="2008-11-30T18:39:49Z"></test-method>
      </class>
    </test>
  </suite>
</testng-results>
'''
        
        def tempFile = File.createTempFile('customized', '.xsl', outputDir)
        def os = new FileOutputStream(tempFile)

        transformer.transform(s, os)
        
        def rows = getMethodRows('Suite1')
        
        assertEquals(10, rows.size())
        
        // by status
        assertEquals('a()', rows[0].td[0].a[0].text())
        assertEquals('b()', rows[1].td[0].a[0].text())
        assertEquals('c()', rows[2].td[0].a[0].text())
        assertEquals('d()', rows[3].td[0].a[0].text())
        assertEquals('e()', rows[4].td[0].a[0].text())
        
        // by class
        assertEquals('a()', rows[5].td[0].a[0].text())
        assertEquals('b()', rows[6].td[0].a[0].text())
        assertEquals('c()', rows[7].td[0].a[0].text())
        assertEquals('d()', rows[8].td[0].a[0].text())
        assertEquals('e()', rows[9].td[0].a[0].text())
        
        tempFile.deleteOnExit()
    }
}