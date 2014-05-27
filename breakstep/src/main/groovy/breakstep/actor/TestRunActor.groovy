package breakstep.actor

import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.UntypedActor
import breakstep.message.TestComplete
import breakstep.message.TestWork
import breakstep.message.TestWorkNeeded
import com.googlecode.lightest.core.LightestTestResult
import com.googlecode.lightest.distributed.TestResultsSanitizer
import com.googlecode.lightest.distributed.TestRunAgent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.concurrent.duration.Duration

import java.util.concurrent.TimeUnit

class TestRunActor extends UntypedActor {
    final static Logger log = LoggerFactory.getLogger(TestRunActor.class)
    TestRunAgent testRunAgent
    Cancellable baseCancel
    ActorRef suiteD
    TestResultsSanitizer sanitizer
    String hostName

    TestRunActor(TestRunAgent testRunAgent, ActorRef suiteDispatcher){
        this.testRunAgent = testRunAgent
        this.suiteD = suiteDispatcher
        this.sanitizer = new TestResultsSanitizer()
        askForWork()
        hostName = InetAddress.localHost.hostName
    }

    @Override
    void onReceive(Object message) throws Exception {
        if (message instanceof TestWork){
            baseCancel.cancel()
            log.info("Working on ${message.byClass.className} ...")

            List<LightestTestResult> ltrs = testRunAgent.runTests([message.byClass.className])

            List<LightestTestResult> sanitized = []

            ltrs.each {sanitized << sanitizer.sanitize(it, message.id)}

            TestComplete comp = new TestComplete(sanitized, message.byClass)
            comp.nodeHostName = hostName
            sender.tell(comp, self)

            askForWork()
        } else if ("touch base".equals(message)){
            askForWork()
        } else {
            unhandled(message)
        }
    }

    private void askForWork() {
        suiteD.tell(new TestWorkNeeded(testRunAgent.env), self)
        scheduleBaseTouch()
    }

    void scheduleBaseTouch() {
        def system = context.system()
        baseCancel =
                system.scheduler().scheduleOnce(Duration.create(5, TimeUnit.SECONDS), self, "touch base", system.dispatcher(),
                                                     ActorRef.noSender())
    }
}
