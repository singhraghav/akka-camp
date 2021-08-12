package actor_intro

import actor_intro.Exercise1.BankAccount.{Deposit, Failure, Success, Withdraw}
import actor_intro.Exercise1.Counter.{Decrement, Increment, PrintCounter}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Exercise1 extends App {

  val system = ActorSystem("Exercise-System")

  class Counter extends Actor {
    var count = 0
    override def receive: Receive = {
    case Increment => count += 1
    case Decrement => count -= 1
    case PrintCounter => println(s"[counter] $count")
    }
  }

  object Counter {
    sealed trait CounterMessage
    case object Increment extends CounterMessage
    case object Decrement extends CounterMessage
    case object PrintCounter extends CounterMessage
  }

  class BankAccount extends Actor {
    var balance: BigDecimal = 0
    override def receive: Receive = {
      case Deposit(amount) =>
        balance += amount
        println(s"[${self.path.name} Account] - $$ $amount Deposited")
        context.sender() ! Success(s"$$ $amount credited successfully")
      case Withdraw(amount) =>
        if(balance - amount >= 0)
          {
            balance -= amount
            println(s"[${self.path.name} Account] - $$ $amount debited")
            context.sender() ! Success(s"$$ $amount withdraw successful")
          }
        else
          context.sender() ! Failure(s"Can't withdraw $amount from ${self.path.name} bank account: Insufficient balance")
      case Success(msg) =>
        println(s"[${self.path.name} bank account] - $msg")
      case Failure(msg) =>
        println(s"[${self.path.name} bank account] - $msg")
    }
  }

  object BankAccount {
    sealed trait BankActions
    case class Deposit(amount: BigDecimal) extends BankActions
    case class Withdraw(amount: BigDecimal) extends BankActions
    case object Statement extends BankActions

    sealed trait BankActionResult
    case class Success(msg: String) extends BankActionResult
    case class Failure(reason: String) extends BankActionResult
  }

  val bobAccount: ActorRef = system.actorOf(Props[BankAccount], "Bob-Account")
  val aliceAccount: ActorRef = system.actorOf(Props[BankAccount], "Alice-Account")

  bobAccount.!(Deposit(100))(aliceAccount)
  aliceAccount.!(Deposit(200))(bobAccount)
  aliceAccount.!(Withdraw(100))(bobAccount)
}
