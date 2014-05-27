package breakstep.message

import com.googlecode.lightest.core.LightestTestResult
import com.googlecode.lightest.core.TestInstancesByClass

class TestComplete implements Serializable {
    private static final long serialVersionUID = 1L
    def nodeHostName
    final TestInstancesByClass byClass
    final List<LightestTestResult> results

    TestComplete(List<LightestTestResult> results, TestInstancesByClass byClass){
        this.byClass = byClass
        this.results = results
    }
}
