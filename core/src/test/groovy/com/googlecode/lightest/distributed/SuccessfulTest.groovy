package com.googlecode.lightest.distributed

import com.googlecode.lightest.core.LightestTestCase
import com.googlecode.lightest.core.SimpleApi
import org.testng.annotations.Test

public class SuccessfulTest extends LightestTestCase{
    SuccessfulTest(){
        def api = new SimpleApi()
        api.addPackage('com.googlecode.lightest.distributed')
        setApi(api)
    }

    @Test
    void makeItSucceed() {
        SuccessTask ()
    }
}
