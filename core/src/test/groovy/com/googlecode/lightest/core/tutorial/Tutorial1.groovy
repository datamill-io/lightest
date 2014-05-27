package com.googlecode.lightest.core.tutorial

import org.testng.annotations.*

class Tutorial1 extends TutorialBase {
    
    @Test
    void sayHello() {
        HelloWorld ()
    }
    
    @Test
    void sayGreeting() {
        HelloWorld (greeting: "Top of the mornin', world!")
    }
}
