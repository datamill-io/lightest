package breakstep

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.kernel.Bootable
import akka.pattern.Patterns
import breakstep.actor.Supervisor
import breakstep.message.StartSuite
import breakstep.message.SuiteComplete
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.Some
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import java.util.concurrent.TimeUnit

class Boot implements Bootable {
    static Logger log = LoggerFactory.getLogger(Boot.class)
    static String actual
    static String suiteFile
    final ActorSystem system = ActorSystem.create("Breakstep")
    ActorRef supervisor

    static void main(String[] args){
        actual = args[0]
        if (args.length > 1){
            suiteFile = args[1]
        }

        Bootable b = new Boot()
        b.startup()

        addShutdownHook {
            b.shutdown()
        }
    }

    @Override
    void startup() {
        supervisor = system.actorOf(Props.create(Supervisor.class, actual), "supervisor")
        def thirtySeconds = Duration.create(60, TimeUnit.SECONDS)
        Future f = Patterns.ask(supervisor, "start", thirtySeconds.toMillis())
        try {
            def res = Await.result(f, thirtySeconds)
            if ("failed".equals(res)) {
                log.error("Couldn't start lightest.")
                System.exit(500)
            }
        } catch (Exception ex){
            log.error("Couldn't start lightest.", ex)
            System.exit(500)
        }
        if (suiteFile){
//            while(true) {
                supervisor.tell(new StartSuite(suiteFile), ActorRef.noSender())
//                def res = Await.result(fut, thirtySeconds)
//                if (!(res instanceof SuiteComplete)){
//                    System.exit(-1)
//                }
//                Thread.sleep(5000)
//                System.exit(0)
//            }
            //supervisor.tell(new StartSuite(suiteFile), ActorRef.noSender())
        }
    }

    @Override
    void shutdown() {
        system.shutdown()
    }
}