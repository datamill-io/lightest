package com.googlecode.lightest.core.filter

import com.googlecode.lightest.core.filter.TestFilterParser

/**
 * Initial template based on http://www.antlr.org/wiki/display/ANTLR3/Test-Driven+Development+with+ANTLR
 */
class TestFilterGrammarTest extends GroovyTestCase {
    def factory
    
    @Override
    void setUp() {
        factory = new ParserFactory()
    }
    
    void testAnnotationExpr() {
        def tests = [
            [ '@Annotation', 'Annotation', "" ],
            [ '@Annotation.member' , 'Annotation', 'member']
        ]
        
        for (test in tests) {
            def parser = factory.createParser(test[0])
            def result = parser.annotationExpr()
            
            assertEquals(test[1], test[1], result.field.name)
            assertEquals(test[2], test[2], result.field.member)
        }
    }
    
    void testHappyPathExpressionRoundTrip() {
        def tests = [
            '@a == "a"', 
            '@a =~ "a"',
            
            '@a.b == "a"',
            
            '@a == "a" || @b == "b"',
            '@a == "a" && @b == "b"',
            
            '@a == "a" || @b == "b" || @c == "c"',
            '@a == "a" && @b == "b" && @c == "c"',
            '(@a == "a" && @b == "b") || @c == "c"',
            '(@a == "a" || @b == "b") && @c == "c"',
            
            '@a == "a" && (@b == "b" || @c == "c")',
            '@a == "a" || (@b == "b" && @c == "c")',
            
            '! @a == "a"',
            '! @a == "a" && @b == "b"',
            '! @a == "a" || @b == "b"',
            '@a == "a" && ! @b == "b"',
            '@a == "a" || ! @b == "b"',
            
            '! (@a == "a" && @b == "b")',
            '! (@a == "a" && @b == "b") || @c == "c"',
            '! (@a == "a" || @b == "b")',
            '! (@a == "a" || @b == "b") && @c == "c"',
            '@a == "a" && ! (@b == "b" || @c == "c")',
            '@a == "a" || ! (@b == "b" && @c == "c")',
        ]
        
        for (test in tests) {
            def parser = factory.createParser(test)
            def result = parser.expr();
            //println ">>>> ${test}"
            def filterString = result.filter.filterString()
            
            assertEquals(test, filterString)
        }
    }
    
    /**
     * These tests should not be considered proper expressions by the parser.
     */
    void testBadGrammar() {
        def tests = [
            '@a'
        ]
        
        for (test in tests) {
            def parser = factory.createParser(test)
            def result = parser.expr();
            
            assertNull(test, result.filter)
        }
    }
    
    void testEscapesInComparisonValue() {
        def tests = [
            '@a == "\\"a\\""'
        ]
        
        for (test in tests) {
            def parser = factory.createParser(test)
            def result = parser.expr();
            
            assertEquals(test, test, result.filter.filterString())
        }
    }
}
