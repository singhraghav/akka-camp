package actor_intro

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object BehaviourExercise extends App {

  val system = ActorSystem("Exercise-System")

  class Counter extends Actor {
    import Counter._
    override def receive: Receive = initialState

    def initialState: Receive = {
      case Increment => context.become(runningState(1))
      case Decrement => context.become(runningState(-1))
      case PrintCounter => println(s"[counter] 0")
    }

    def runningState(value: Int): Receive = {
      case Increment => context.become(runningState(value + 1))
      case Decrement => context.become(runningState(value - 1))
      case PrintCounter => println(s"[counter] $value")
    }
  }

  object Counter {
    sealed trait CounterMessage
    case object Increment extends CounterMessage
    case object Decrement extends CounterMessage
    case object PrintCounter extends CounterMessage
  }

  case class Vote(candidate: String)

  class Citizen extends Actor {
    def receive: Receive = initialState

    def initialState: Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
      case Vote(candidate) => context.become(votedState(candidate))
    }

    def votedState(votedFor: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(votedFor))
    }
  }

  case class AggregateVote(citizens: Set[ActorRef])
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  case object ShowVoteResult

  class VoteAggregator extends Actor {
    def receive: Receive = {
      case AggregateVote(citizens) => citizens.foreach(_ ! VoteStatusRequest)
                                      context.become(accumulatorState(Map.empty[String, Int]))
      case ShowVoteResult          => println(s"Result Has Not Been Calculated Yet")
    }

    def accumulatorState(voteStats: Map[String, Int]): Receive = {
      case VoteStatusReply(mayBeCandidate) =>
        val updatedStats: Map[String, Int] = mayBeCandidate.map{ candidate =>
          val countForCandidate: Int = voteStats.getOrElse(candidate, 0) + 1
          voteStats + (candidate -> countForCandidate)
        }.getOrElse(voteStats)
        println(s"***** Vote Result *****")
        updatedStats.foreach{
          case (candidate, vote) => println(s"$candidate - $vote")
        }
        println(s"***********************")
        context.become(accumulatorState(updatedStats))
    }
  }

  val alice = system.actorOf(Props[Citizen], "alice")
  val bob = system.actorOf(Props[Citizen], "bob")
  val charlie = system.actorOf(Props[Citizen], "charlie")
  val daniel = system.actorOf(Props[Citizen], "daniel")

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator], "aggregator")
  voteAggregator ! AggregateVote(Set(alice, bob, charlie, daniel))
}
