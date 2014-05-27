package com.googlecode.lightest.core.tutorial

import org.testng.annotations.*

class Tutorial2 extends TutorialBase {
    static final GREETINGS = [
         "Top of the mornin', world!"
         , "Howdy, world!"
         , "G'day mate, world!"
         , "Bonjour, world!"
     ]
    
    @Test
    void sayGreeting() {
        QueryWorld ()
        HelloWorld (greeting: "Top of the mornin', world!")
    }
    
    @Test
    void sayGreeting2() {
        QueryWorld (description: 'Find a world, and greet it internationally') {
            for (g in GREETINGS) {
                HelloWorld (greeting: g)
            }
        }
    }
}
