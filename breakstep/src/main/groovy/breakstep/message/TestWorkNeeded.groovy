package breakstep.message

import com.googlecode.lightest.core.ITestEnvironment

class TestWorkNeeded implements Serializable {
    private static final long serialVersionUID = 1L
    ITestEnvironment env
    TestWorkNeeded(def env){
        this.env = env
    }
}
