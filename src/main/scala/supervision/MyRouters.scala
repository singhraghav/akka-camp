package supervision

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinPool, RoundRobinRoutingLogic, Router}

import java.lang.Exception

object MyRouters extends App {

  // master - slave

  /*
  * 1. Manually created router
  * */
  class Master extends Actor{

    override val supervisorStrategy = OneForOneStrategy(){
      case ex: Exception =>
        println(s"exception came to master ${ex.getMessage}")
        Escalate
    }
    //1.Step 1 create routes
    private val slaves = for (i <- 1 to 5) yield {
      val newSlave = context.actorOf(Props[Slave], s"Slave_$i")
      context.watch(newSlave)
      ActorRefRoutee(newSlave)
    }
    //Step 2 Create router
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    //Step 3 Pass on the msgs to router
    //Step 4 handle termination of routees
    override def receive: Receive = {
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
      case msg => router.route(msg, sender())
    }
  }

  class Slave extends Actor with ActorLogging{

    override def preStart(): Unit = log.info(s"started Slave ${context.self.path.name}")

    override def receive: Receive = {
      case msg => log.info(msg.toString)
        throw new Exception(s"Routee throws an exception ${context.self.path.name}")
    }

  }

  val system = ActorSystem("RouterSystem")
  val master = system.actorOf(Props[Master])
//  master ! "Hey there master"


  /**
   * a router actor with its own children
   * */

  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")
  poolMaster ! "Hey there pool master"
}
