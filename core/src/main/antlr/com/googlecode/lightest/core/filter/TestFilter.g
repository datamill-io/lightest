grammar TestFilter;

options {
    output = AST;
}

@parser::header {
package com.googlecode.lightest.core.filter;
}

@lexer::header {
package com.googlecode.lightest.core.filter;
}

@members {
    protected String trimQuotes(String s) {
        int end = s.length() - 1;
        return s.substring(1, end);
    }
}

/* parser rules */

expr returns [ITestFilter filter]
    : a=filterExpr { $filter = a.filter; }
    ;

filterExpr returns [ITestFilter filter]
    : a=orExpr { $filter = a.filter; }
    | b=notExpr { $filter = b.filter; }
    ;

notExpr returns [NotFilter filter]
    : '!' a=filterExpr { $filter = new NotFilter(a.filter); }
    ;

orExpr returns [ITestFilter filter]
    : a=andExpr ('||' b=filterExpr)? {
        $filter = (b == null)
            ? a.filter
            : new OrFilter(a.filter, b.filter);
    }
    ;

andExpr returns [ITestFilter filter]
    : a=atom ('&&' b=filterExpr)? {
        $filter = (b == null)
            ? a.filter
            : new AndFilter(a.filter, b.filter);
    }
    ;

atom returns [ITestFilter filter]
    : a=compareExpr { $filter = a.filter; }
    | b=parExpr { $filter = b.filter; }
    ;

compareExpr returns [ITestFilter filter]
    : a=equalsExpr { $filter = a.filter; }
    | b=containsExpr { $filter = b.filter; }
    ;

equalsExpr returns [AnnotationEqualsFilter filter]
    : a=annotationExpr '==' b=StringLiteral {
        $filter = new AnnotationEqualsFilter();
        $filter.setField(a.field);
        $filter.setValue(trimQuotes($b.text));
    }
    ;

containsExpr returns [AnnotationContainsFilter filter]
    : a=annotationExpr '=~' b=StringLiteral {
        $filter = new AnnotationContainsFilter();
        $filter.setField(a.field);
        $filter.setValue(trimQuotes($b.text));
    }
    ;

annotationExpr returns [AnnotationField field]
    : '@' a=Identifier ('.' b=Identifier)? {
        $field = (b == null)
            ? new AnnotationField($a.text)
            : new AnnotationField($a.text, $b.text);
    }
    ;

parExpr returns [ITestFilter filter]
    : '(' a=filterExpr ')' { $filter = new FilterGroup(a.filter); }
    ;

// below is courtesy of ANTLR v3 Java.g example

fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

StringLiteral
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;

fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
    |   UnicodeEscape
    |   OctalEscape
    ;

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

Identifier 
    :   Letter (Letter|JavaIDDigit)*
    ;

/**I found this char range in JavaCC's grammar, but Letter and Digit overlap.
   Still works, but...
 */
fragment
Letter
    :  '\u0024' |
       '\u0041'..'\u005a' |
       '\u005f' |
       '\u0061'..'\u007a' |
       '\u00c0'..'\u00d6' |
       '\u00d8'..'\u00f6' |
       '\u00f8'..'\u00ff' |
       '\u0100'..'\u1fff' |
       '\u3040'..'\u318f' |
       '\u3300'..'\u337f' |
       '\u3400'..'\u3d2d' |
       '\u4e00'..'\u9fff' |
       '\uf900'..'\ufaff'
    ;

fragment
JavaIDDigit
    :  '\u0030'..'\u0039' |
       '\u0660'..'\u0669' |
       '\u06f0'..'\u06f9' |
       '\u0966'..'\u096f' |
       '\u09e6'..'\u09ef' |
       '\u0a66'..'\u0a6f' |
       '\u0ae6'..'\u0aef' |
       '\u0b66'..'\u0b6f' |
       '\u0be7'..'\u0bef' |
       '\u0c66'..'\u0c6f' |
       '\u0ce6'..'\u0cef' |
       '\u0d66'..'\u0d6f' |
       '\u0e50'..'\u0e59' |
       '\u0ed0'..'\u0ed9' |
       '\u1040'..'\u1049'
    ;

WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;


