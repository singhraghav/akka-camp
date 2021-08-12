package actor_intro

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App{

  class SimpleActor extends Actor{
    context.self // the whole reference -> path of whole actor
    val name = self.path.name
    def receive: Receive = {
      case "Hi" => println(s"[$name] reply to Hi!!")
        context.sender() ! "Hello There"
      case msg: String => println(s"[$name] received $msg")
      case i: Int => println(s"[$name] received a number $i")
      case spl: MyMessage => println(s"[$name] received a special message $spl")
      case SAyHiTo(ref) => println(s"[$name] received SAyHiTo")
        ref ! "Hi"
    }
  }

  val system = ActorSystem("ActorCap")

  val simpleActor1 = system.actorOf(Props[SimpleActor], "sa-1")
  simpleActor1 ! "hello simple actor"

  //messages can be of any type

  simpleActor1 ! 12

  case class MyMessage(contents: String)

  simpleActor1 ! MyMessage("i am special")
  //2. messages must be immutable
  //3. messages must be serializable -> i.e message should be convertible to byte code

  //2. Actor have info about themselves
  // each actor has context which has info about the actor

  // actor can reply to messges

  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SAyHiTo(ref: ActorRef)
  alice ! SAyHiTo(bob)
  alice ! "Hi"









}
