package actor_intro

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.Logging

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.collection.mutable

object ChildActorExercise extends App {

  class WordCounterMaster extends Actor with ActorLogging {
    import WordCounterMaster._
    override def receive: Receive = initialState

    def initialState: Receive = {
      case WordCountTask(_, _) => log.info(s"[master] can't calculate count no worker initialised")
      case WordCountReply(_, _ , _) => log.info(s"[master] word count received in initial state. Bug in state transition")
      case Initialize(n) =>
        val workers = initializeWorkers(n, List.empty[ActorRef], context)
        context.become(workingState(workers, Queue.empty[WordCountTask]))
    }

    def workingState(freeWorkers: List[ActorRef], taskToBeDone: Queue[WordCountTask]): Receive = {
      case task: WordCountTask =>
        if (freeWorkers.isEmpty) {
          log.info(s"[master] no free worker available enquing to queue $task")
          context.become(workingState(freeWorkers, taskToBeDone.enqueue(task)))
        }
        else {
          val taskAssignedTo = freeWorkers.head
          val (taskToExecute, updatedQueue) = taskToBeDone.enqueue(task).dequeue
          log.info(s"[master] assigning ${taskToExecute.text} to ${taskAssignedTo.path.name}")
          taskAssignedTo ! taskToExecute
          context.become(workingState(freeWorkers.tail, updatedQueue))
        }
      case WordCountReply(word, count, replyTo) =>
        replyTo ! s"$word - $count"
        if (taskToBeDone.isEmpty) {
          log.info(s"[master] no next task available adding ${sender().path.name} to free workers")
          context.become(workingState(freeWorkers :+ sender(), taskToBeDone))
        }
        else {
          val taskAssignedTo = freeWorkers.headOption
          val (nextTask, updatedTaskQueue) = taskToBeDone.dequeue
          taskAssignedTo match {
            case Some(freeActor) =>
              log.info(s"[master] adding ${sender().path.name} to free workers")
              log.info(s"[master] next task ${nextTask.text} assigned to ${freeActor.path.name}")
              freeActor ! nextTask
              context.become(workingState(freeWorkers.tail :+ sender(), updatedTaskQueue))
            case None =>
              log.info(s"[master] next task ${nextTask.text} assigned to ${sender().path.name}")
              sender() ! nextTask
              context.become(workingState(freeWorkers, updatedTaskQueue))
          }
        }
    }

    @tailrec
    private def initializeWorkers(n: Int, workers: List[ActorRef], parentContext: ActorContext): List[ActorRef] = {
      if(n == 0 )
        workers
      else {
        val childActor: ActorRef = parentContext.actorOf(Props[WordCounterWorker], s"worker_$n")
        initializeWorkers(n-1, childActor :: workers, parentContext)
      }
    }
  }

  object WordCounterMaster {
    case class Initialize(nChildren: Int) // in response create n workers
    case class WordCountTask(text: String, replyTo: ActorRef)
    case class WordCountReply(word: String, count: Int, replyTo: ActorRef)
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(text, replyTo) =>
        val count = text.split(" ").length
        sender() ! WordCountReply(text, count, replyTo)
    }
  }

  // 1. WordCountTask is received before any worker is initialized
  // 2. task more than the workers are there

  class Person extends Actor {
    def receive: Receive = {
      case msg => println(s"[person] received $msg")
    }
  }

  val system = ActorSystem("childSystem")
  val master = system.actorOf(Props[WordCounterMaster])
  val person = system.actorOf(Props[Person])
  import WordCounterMaster._
  master ! WordCountTask("Hey There Count the words", person)
  master ! Initialize(3)
  master ! WordCountTask("Hey There Count the words one",  person)
  master ! WordCountTask("Hey There Count the words two", person)
  master ! WordCountTask("Hey There Count the words three",  person)
  master ! WordCountTask("Hey There Count the words four",  person)
  master ! WordCountTask("Hey There Count the words five",  person)
  Thread.sleep(1000)
  master ! WordCountTask("Hey There Count the words six",  person)
  master ! WordCountTask("Hey There Count the words seven",  person)
}
