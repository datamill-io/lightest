package breakstep.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.UntypedActor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent
import akka.contrib.pattern.ClusterSingletonManager
import akka.contrib.pattern.ClusterSingletonProxy
import breakstep.message.StartSuite
import breakstep.message.StopCluster
import com.googlecode.lightest.distributed.TestReportAgent
import com.googlecode.lightest.distributed.TestRunAgent
import com.googlecode.lightest.distributed.TestSuiteHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Supervisor extends UntypedActor{
    private static Logger log = LoggerFactory.getLogger(Supervisor.class)
    ActorRef suiteDispatcher
    ActorRef reportDispatcher

    Cluster cluster = Cluster.get(getContext().system())
    ActorRef testRunActor
    String actual

    Supervisor (String actualConfig){
        this.actual = actualConfig
    }

    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.MemberUp.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    void onReceive(Object message) throws Exception {
        if ("start".equals(message)) {
            try {
                startReportWriter()
                startSuiteDispatcher()
                startTestRunActor(suiteDispatcher)
                log.info("Actor System Started")
                sender.tell("success", self)
            } catch(Exception ex){
                log.error("Failed to start", ex)
                sender.tell("failed", self)
            }
        } else if (message instanceof ClusterEvent.CurrentClusterState) {
            message.members.each(){ member ->
                log.info ("Witnessed state of ${member.status()} for ${member.address()}")
            }
        } else if (message instanceof ClusterEvent.MemberUp) {
        } else if (message instanceof ClusterEvent.MemberRemoved) {
        } else if (message instanceof StartSuite){
            suiteDispatcher.forward(message, context)
        } else {
            unhandled(message)
        }
    }

    private void startReportWriter() {
        TestReportAgent tra = new TestReportAgent()
        tra.configure(new File(actual))
        def reportDispatcherManager = getContext().system().actorOf(
                ClusterSingletonManager.defaultProps(Props.create(ReportDispatcher.class, tra), "report-dispatcher",
            new StopCluster(), "test-worker"), "singleton-report");
        reportDispatcher = context.system().actorOf(ClusterSingletonProxy.defaultProps("/user/singleton-report/report-dispatcher", "test-worker"), "report-proxy")
    }

    private void startSuiteDispatcher() {
        TestSuiteHolder eric = new TestSuiteHolder()
        eric.configure(new File(actual))
        def suiteDispatcherManager = getContext().system().actorOf(
                ClusterSingletonManager.defaultProps(Props.create(SuiteDispatcher.class, eric, reportDispatcher), "suite-dispatcher",
                new StopCluster(), "test-worker"), "singleton-suite");

        suiteDispatcher = context.system().actorOf(ClusterSingletonProxy.defaultProps("/user/singleton-suite/suite-dispatcher", "test-worker"), "suite-proxy")
    }

    private void startTestRunActor(ActorRef suiteD) {
        TestRunAgent tra = new TestRunAgent()
        tra.configure(new File(actual))
        testRunActor = getContext().system().actorOf(Props.create(TestRunActor.class, tra, suiteD), "test-runner")
    }
}
