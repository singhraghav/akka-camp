package supervision

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

object MyMailBox extends App {
    val system = ActorSystem("MailBoxDemo")

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(s"${message.toString}")
    }
  }

  //Case 1 - Custom Priority Mail Box
  //p0 - most important
  //p1, p2, p3 ...

  val priorityGen = PriorityGenerator{
    case message: String if message.startsWith("[P0]") => 0
    case message: String if message.startsWith("[P1]") => 1
    case message: String if message.startsWith("[P2]") => 2
    case message: String if message.startsWith("[P3]") => 3
    case _ => 4
  }

  //Step 1 - MailBox Definition
  class SupportTicketPriorityMailBox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(priorityGen)

  //Step2 - Make it known in config


  val logger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  logger ! PoisonPill
  Thread.sleep(1000)
  logger ! "[P3] nice to have"
  logger ! "[P0] now"
  logger ! "[P1] when have time"
}
