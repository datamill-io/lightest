package com.googlecode.lightest.core;

import org.testng.xml.Parser

public class ParserFactory {

    Parser newParser(String fileName) {
        return new Parser(fileName)
    }
}