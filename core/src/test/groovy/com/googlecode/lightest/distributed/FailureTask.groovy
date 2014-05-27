package com.googlecode.lightest.distributed

import com.googlecode.lightest.core.ITaskResult
import com.googlecode.lightest.core.LightestTask

class FailureTask extends LightestTask{
    @Override
    void doPerform(ITaskResult result) {
        result.fail()
    }
}
