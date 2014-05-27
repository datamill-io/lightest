package com.googlecode.lightest.core.tutorial

import org.testng.annotations.*

class Tutorial5 extends TutorialBase {

    @Test
    void helloGoodbye() {
        GreetWorld () {
            GoodbyeWorld (lastWords: 'thanks for all the fish')
        }
    }
}
