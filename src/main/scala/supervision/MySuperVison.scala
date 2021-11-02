package supervision

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, Terminated}

object MySuperVision extends App {
  case object Report
  class FussyWordCounter extends Actor with ActorLogging {
    var words = 0;

    override def receive: Receive = {
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String =>
        if(sentence.length > 20) throw new RuntimeException("sentence too big")
        else if(!Character.isUpperCase(sentence(0)))
          throw new IllegalArgumentException("sentence must start with an Uppercase")
        else
          words += sentence.split(" ").length
      case Report => sender() ! words
      case msg =>
        log.info(s"Received message $msg other than string in Work Counter")
        throw new Exception("can only receive strings")
    }
  }

  class SuperVisor extends Actor with ActorLogging {

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info(s"Restarting supervisor")
    }

    override val supervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props: Props =>
        log.info("received props in supervisor")
        val childRef = context.actorOf(props)
        context.watch(childRef)
        sender() ! childRef
      case Terminated(_) =>
        log.info(s"Word Counter Eliminated")
    }
  }

  case class MonitorSuperVisor(actorRef: ActorRef)
  case object Other
  class Processor extends Actor with ActorLogging {

    override def receive: Receive = {
      case MonitorSuperVisor(ref) =>
        log.info(s"Changing handler to superVisorCreated")
        context.become(superVisorCreated(ref))
    }

    def superVisorCreated(supervisor: ActorRef): Receive = {
      case ref: Props =>
        log.info(s"sending props to supervisor")
        supervisor ! ref
      case actorRef: ActorRef =>
        log.info(s"received $actorRef")
        log.info(s"changing to state wordCounterEnabled")
        context.become(wordCounterEnabled(supervisor, actorRef))
    }

    def wordCounterEnabled(superVisorRef: ActorRef, wordCounterRef: ActorRef): Receive = {
      case word: String =>
        log.info(s"received $word in processor")
        wordCounterRef ! word
      case count: Int =>
        println(s"Word Counter has $count words")
      case Report => wordCounterRef ! Report
      case other =>
        log.info(s"$other received by processor")
        wordCounterRef ! other
    }
  }

  val actorSystem = ActorSystem("supervisonStrat")

  val superVisor = actorSystem.actorOf(Props[SuperVisor], "supervisor")
  val processor = actorSystem.actorOf(Props[Processor], "processor")
  val child1 = Props[FussyWordCounter]

  processor ! MonitorSuperVisor(superVisor)
  processor ! child1
  Thread.sleep(500)
  processor ! "Raghav  interview"
  processor ! Report

  processor ! "Raghav  will succeed in the interview gracefully he is great"
  processor ! Report
  processor ! ""
  processor ! Report
//  processor ! "raghav"
//  processor ! Report

  processor ! Other
  processor ! Report
}
