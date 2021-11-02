package supervision

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.{Backoff, BackoffOpts, BackoffSupervisor}

import java.io.File
import scala.io.Source
import scala.concurrent.duration._
import scala.language.postfixOps

object MyBackOffSuperVision extends App {

  val system = ActorSystem("MyBackOffSuperVision")


  case object ReadFile

  class FileBasedPersistenceActor extends Actor with ActorLogging {

    var dataSource: Source = null

    override def preStart(): Unit =
      log.info(s"Persistent Actor Starting")

    override def postStop(): Unit =
      log.warning(s"Persistent actor stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"Persistent Actor Restarting")

    override def receive: Receive = {
      case ReadFile =>
      if(dataSource == null) {
        dataSource = Source.fromFile(new File("./src/main/resources/testFiles/important_data.txt"))
        log.info("I've read important data" + dataSource.getLines().toList)
      }
    }

  }

//  val simpleActor = system.actorOf(Props[FileBasedPersistenceActor], "simpleActor")

//  simpleActor ! ReadFile
  val simpleSuperVisorProps = BackoffSupervisor.props(BackoffOpts.onFailure(
    Props[FileBasedPersistenceActor],
    "simpleBackOffActor",
    3 seconds,
    30 seconds,
    0.2
  ))

  val simpleBackOffSupervisor = system.actorOf(simpleSuperVisorProps, "simpleSuperVisor")
  simpleBackOffSupervisor ! ReadFile

}
