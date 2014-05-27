package com.googlecode.lightest.core.tutorial

import com.googlecode.lightest.core.LightestTestCase
import com.googlecode.lightest.core.SimpleApi

class TutorialBase extends LightestTestCase {
    TutorialBase() {
        def api = new SimpleApi()
        api.addPackage('com.googlecode.lightest.core.tutorial.tasks')
        setApi(api)
    }
}
