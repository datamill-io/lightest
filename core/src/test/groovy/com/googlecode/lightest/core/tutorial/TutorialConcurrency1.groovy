package com.googlecode.lightest.core.tutorial

import org.testng.annotations.*

class TutorialConcurrency1 extends TutorialBase {

    @Test(invocationCount = 50, threadPoolSize = 2)
    void helloGoodbye() {
        GreetWorld () {
            GoodbyeWorld (lastWords: 'thanks for all the fish')
        }
    }
}
