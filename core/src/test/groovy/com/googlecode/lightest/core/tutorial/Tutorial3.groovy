package com.googlecode.lightest.core.tutorial

import org.testng.annotations.*

class Tutorial3 extends TutorialBase {
    public static final List GREETINGS = [
         "Top of the mornin', world!"
         , "Howdy, world!"
         , "G'day mate, world!"
         , "Bonjour, world!"
    ]

    @Test
    void sayGreeting() {
        QueryWorld2 (description: 'Find a world, and greet it internationally') {
            for (g in GREETINGS) {
                HelloWorld (greeting: g)
            }
        }
        HelloWorld (greeting: 'Hello, non-blown-up world!')
    }
    
    @Test
    void sayHelloBeforeChecking() {
        HelloWorld (greeting: 'Hello, non-blown-up world!') {
            QueryWorld2 (description: 'Oops, spoke too soon!')
            HelloWorld (greeting: 'Hello, Smith Areans!')
        }
    }
}
