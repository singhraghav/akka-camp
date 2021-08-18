package actor_intro

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntoAkkaConfig extends App {

  /**
   * inline config
   * */

  val configString =
    """
      |akka {
      | loglevel = "DEBUG"
      |}
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)

//  val system1 = ActorSystem("configsystem1", ConfigFactory.load(config))

  class LoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

//  val loggingActor = system1.actorOf(Props[LoggingActor])
//  loggingActor ! "Message to Remember"
/**
 * Default config
 * */
  val system2 = ActorSystem("configsystem2")
  val loggingActor2 = system2.actorOf(Props[LoggingActor])
  loggingActor2 ! "Message to Remember"
}
