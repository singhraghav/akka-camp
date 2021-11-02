package patterns

import akka.actor.{Actor, ActorLogging, Stash}

object StashDemo extends App {

  case object Open
  case object Read
  case object Close
  case class Write(data: String)

  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""
    override def receive: Receive = ???

    def close: Receive = {
      case Open =>
        log.info(s"Opening Resource")
        unstashAll()
        context.become(open)
      case other =>
        log.info(s"Resource Closed - Stashing $other")
        stash()
    }

    def open: Receive = {
      case Read =>
        log.info(s"I have read $innerData")
      case Write(data) =>
        log.info(s"Writing data $data")
        innerData = data
      case Close =>
        log.info(s"Closing Resource")
        context.become(close)
    }
  }

}
