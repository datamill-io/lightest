package breakstep.actor

import akka.actor.UntypedActor
import breakstep.message.StartSuite
import breakstep.message.TestComplete
import com.googlecode.lightest.distributed.TestReportAgent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ReportDispatcher extends UntypedActor{
    final Logger log = LoggerFactory.getLogger(ReportDispatcher.class)
    TestReportAgent tra

    ReportDispatcher(TestReportAgent tra){
        this.tra = tra
    }

    @Override
    void onReceive(Object message) throws Exception {
        if (message instanceof TestComplete) {
            log.info("Doing report for ${message.byClass.className}")

            def tibc = message.byClass.instances

            message.results.each {
                tra.testComplete(it, tibc[0].testName)
            }
        } else if (message instanceof StartSuite){
            log.info("Starting new suite ${message.suiteName}.")
            tra.startSuite([message.suiteName])
        } else {
            unhandled( message )
        }
    }
}
