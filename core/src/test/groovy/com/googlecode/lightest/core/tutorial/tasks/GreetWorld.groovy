package com.googlecode.lightest.core.tutorial.tasks

import com.googlecode.lightest.core.ITaskResult
import com.googlecode.lightest.core.LightestTask

class GreetWorld extends LightestTask {
    static final GREETINGS = [
        "Top of the mornin', world!"
        , "Howdy, world!"
        , "G'day mate, world!"
        , "Bonjour, world!"
    ]

    void doPerform(ITaskResult result) {
        QueryWorld3 (description: 'Find a world, and greet it internationally') {
            for (g in GREETINGS) {
                HelloWorld (greeting: g)
            }
        }
        HelloWorld (greeting: 'Hello, non-blown-up world!')
    }
}