package com.googlecode.lightest.core.filter

import org.antlr.runtime.*

/**
 * Returns TestFilter parsers for input Strings*/
public class ParserFactory {

    /**
     * Returns a parser for the given input string.
     *
     * @param input
     */
    TestFilterParser createParser(String input) {
        CharStream stream = new ANTLRStringStream(input)
        TestFilterLexer lexer = new TestFilterLexer(stream)
        CommonTokenStream tokens = new CommonTokenStream(lexer)
        TestFilterParser parser = new TestFilterParser(tokens)

        return parser
    }
}
