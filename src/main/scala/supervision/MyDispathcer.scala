package supervision

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.util.Random

object MyDispathcer extends App {

  class Counter extends Actor with ActorLogging {
    var count = 0
    override def receive: Receive = {
      case msg =>
        count += 1
        log.info(s"${msg.toString} - $count")
    }
  }

  val system = ActorSystem("DispatcherDemo", ConfigFactory.load().getConfig("dispatcherDemo")) //

//  val actors = for(i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
//
//  val r = new Random()
//  for(i <- 1 to 1000) {
//    actors(r.nextInt(10)) ! i
//  }

  val actor2 = system.actorOf(Props[Counter], "rtjvm")
}
