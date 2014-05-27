package breakstep.message

import com.googlecode.lightest.core.TestInstancesByClass

class TestWork implements Serializable {
    private static final long serialVersionUID = 1L
    final TestInstancesByClass byClass
    final int id

    TestWork(TestInstancesByClass byClass, int id) {
        this.byClass = byClass
        this.id = id
    }
}
