package com.googlecode.lightest.distributed;

import com.googlecode.lightest.core.LightestTestResult;
import com.googlecode.lightest.report.dto.TestResultDTO;

public class TestResultsSanitizer {
    public LightestTestResult sanitize(LightestTestResult ltr, int id){
        TestResultDTO trd = new TestResultDTO(ltr.getResult());

        LightestTestResult ret = new LightestTestResult(trd, id);
        ret.setEnv(ltr.getEnv());
        ret.setTaskResults(ltr.getTaskResults());
        return ret;
    }
}
