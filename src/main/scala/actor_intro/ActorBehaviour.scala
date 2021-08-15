package actor_intro

import akka.actor.Actor

object ActorBehaviour extends App {

 class StateLesskid extends Actor {
   def receive: Receive = happyReceive

   def happyReceive: Receive = {
     case "Veggies" => context.become(sadReceive)
     case "Choclate" => ???
     case "Ask" => sender() ! "Accept"
   }

   def sadReceive: Receive = {
     case "Veggies" => ???
     case "Choclate" => context.become(happyReceive)
     case "Ask" => sender() ! "Reject"
   }
 }
}
