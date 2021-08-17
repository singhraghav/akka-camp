package actor_intro

import akka.actor.{Actor, ActorRef, Props}

object ChildActors extends App {

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {
    import Parent._
    def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(handlerWithChild(childRef))
    }

    def handlerWithChild(child: ActorRef): Receive = {
      case TellChild(msg) => child forward msg
    }
  }

  class Child extends Actor {
    def receive: Receive = {
      case message => println(s"${self.path} I got $message")
    }
  }
}
