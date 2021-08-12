package actor_intro

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorIntro extends App {
  // part1- actor system
  val actorSystem = ActorSystem("IntroActorSystem")
  println(actorSystem.name)
  //part2- create actor
  //word count actor

  class WordCountActor extends Actor {
    // internal data/state
    var totalWords = 0

    // behaviour
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word counter] I have received $message")
        totalWords += message.split(" ").length
      case msg => println(s"[Word Counter] can't understand $msg")
    }
  }

  //part3 - instantiate our actor
  val wordCounter: ActorRef = actorSystem.actorOf(Props[WordCountActor], "wordCounter")

  //part 4 - communicate -> we can only talk through actor ref
  wordCounter ! "I am learning akka, thanks to udemy"

  //message is sent asynchronously

  //creating an actor with constructor

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case msg => println(s"Received $msg")
    }
  }

  val person = actorSystem.actorOf(Props(new Person("Raghav")), "Raghav") // disregarded
  person ! "Hi Raghav"

  //best practice - use a companion object to create props instances
  object Person{
    def props(name: String) = Props(new Person(name))
  }

  val person2 = actorSystem.actorOf(Person.props("second_person"))
}
