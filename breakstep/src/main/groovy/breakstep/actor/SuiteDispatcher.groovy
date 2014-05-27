package breakstep.actor

import akka.actor.ActorRef
import akka.actor.UntypedActor
import breakstep.message.StartSuite
import breakstep.message.SuiteComplete
import breakstep.message.SuiteRejection
import breakstep.message.TestComplete
import breakstep.message.TestTimedOut
import breakstep.message.TestWork
import breakstep.message.TestWorkNeeded
import com.googlecode.lightest.core.ITestEnvironment
import com.googlecode.lightest.core.TestInstancesByClass
import com.googlecode.lightest.distributed.TestSuiteHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SuiteDispatcher extends UntypedActor {
    static final Logger log = LoggerFactory.getLogger(SuiteDispatcher.class)
    TestSuiteHolder holder
    List<TestTracker> allTests = []
    Set<TestTracker> pendingTests = new HashSet<>()
    Set<TestTracker> currentTests = new HashSet<>()
    Iterator<TestTracker> testIterator
    ActorRef reportDispatcher
    ActorRef initiator

    Set<TestWorkEnv> waitingWorkEnvs = [] as Set
    int nextId
    String suiteName

    SuiteDispatcher(TestSuiteHolder holdr, ActorRef reportDispatcher){
        this.holder = holdr
        this.reportDispatcher = reportDispatcher
    }

    @Override
    void onReceive(Object message) throws Exception {
        if (message instanceof StartSuite) {
            if (!pendingTests.isEmpty()){
                log.warn("Rejecting Suite ${message.suiteName}")
                sender.tell(new SuiteRejection(message.suiteName), self)
            }

            startNewSuite(message.suiteName)

            initiator = sender

            waitingWorkEnvs.each { workEnv ->
                if (testIterator && testIterator.hasNext()){
                    issueTestToWorker(workEnv.worker)
                }
            }
            waitingWorkEnvs.clear()
        } else if (message instanceof TestWorkNeeded) {
            if (testIterator && testIterator.hasNext()) {
                issueTestToWorker(sender)
            } else {
                waitingWorkEnvs << new TestWorkEnv(message.env, sender)
            }
        } else if (message instanceof TestComplete) {
            log.info("${message.nodeHostName} completed ${message.byClass.className}")

            def testClass = message.byClass

            def tt = currentTests.find { it.byClass == testClass }

            if (tt != null){
                currentTests.remove(tt)
                tt.state = TestState.completed
            }

            reportDispatcher.tell(message, self)

            if (pendingTests.isEmpty() && currentTests.isEmpty()) {
                log.info("All done with suite ${suiteName}.")
                initiator.tell(new SuiteComplete(), self)
            }
        } else if (message instanceof TestTimedOut) {
            log.info("${message.nodeHostName} timed out on ${message.testName} which kind of sucks")
        } else {
            unhandled(message)
        }
    }

    private void issueTestToWorker(ActorRef worker) {
        log.info("Issuing work to ${worker}")
        def tt = testIterator.next()
        pendingTests.remove(tt)
        currentTests.add(tt)
        tt.worker = worker
        worker.tell(new TestWork(tt.byClass, nextId++), self)
        tt.state = TestState.started
    }

    private void startNewSuite(suiteName) {
        log.info("Received new suite ${suiteName}")
        allTests = []
        nextId = 1
        this.suiteName = suiteName

        pendingTests = new HashSet<>()
        currentTests = new HashSet<>()

        List<TestInstancesByClass> testNames = holder.getTestInstancesByClass([suiteName])
        testNames.each { testInstancesByClass ->
            def tt = new TestTracker(testInstancesByClass)
            allTests << tt
            pendingTests << tt
        }

        log.info("Starting Suite ${suiteName}; ${allTests.size()} allTests discovered.")
        reportDispatcher.tell(new StartSuite(suiteName), self)
        testIterator = allTests.iterator()
    }

    class TestTracker {
        TestState state = TestState.pending
        TestInstancesByClass byClass
        ActorRef worker

        TestTracker(TestInstancesByClass testName){
            this.byClass = testName
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            TestTracker that = (TestTracker) o

            if (byClass.className != that.byClass.className) return false

            return true
        }

        int hashCode() {
            return byClass.className.hashCode()
        }
    }

    enum TestState{
        pending,
        started,
        completed
    }

    class TestWorkEnv {
        ITestEnvironment env
        ActorRef worker
        TestWorkEnv(def env, def worker){
            this.env = env
            this.worker = worker
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            TestWorkEnv that = (TestWorkEnv) o

            if (env != that.env) return false
            if (worker != that.worker) return false

            return true
        }

        int hashCode() {
            int result
            result = (env != null ? env.hashCode() : 0)
            result = 31 * result + (worker != null ? worker.hashCode() : 0)
            return result
        }
    }
}
