package com.googlecode.lightest.core.tutorial

import org.testng.annotations.*

class Tutorial4 extends TutorialBase {
    
    @Test
    void sayHelloBeforeChecking() {
        HelloWorld (greeting: 'Hello, non-blown-up world!') {
            QueryWorld3 (description: 'Oops, spoke too soon!')
            HelloWorld (greeting: 'Hello, Smith Areans!')
        }
    }
}
