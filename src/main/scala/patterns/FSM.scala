package patterns

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, FSM}

import scala.concurrent.duration._
import scala.language.postfixOps

object FSM extends App {

  /*
  * Vending Machine
  * */
  //Step - 1 -> Define state and data for the actor
  // 1. idle 2. operational 3. waiting for machine
  class VendingMachine extends Actor with ActorLogging {

    override def receive: Receive = ???
  }

  case class Initialise(inventory: Map[String, Int], prices: Map[String, Int])
  case class Instruction(msg: String)
  case class VendingError(msg: String)
  case class RequestProduct(product: String)
  case object ReceivedMoneyTimeout
  case class ReceivedMoney(amount: Int)
  case class GiveMoneyBack(amount: Int)
  case class Deliver(product: String)

  trait VendingState
  case object Idle extends VendingState
  case object Operational extends VendingState
  case object WaitForMoney extends VendingState

  trait VendingData
  case object Uninitialised extends VendingData
  case class Initialised(inventory: Map[String, Int], prices: Map[String, Int]) extends VendingData
  case class WaitForMoneyData(inventory: Map[String, Int],
                              prices: Map[String, Int],
                              product: String,
                              money: Int,
                              requester: ActorRef
                             ) extends VendingData

  class VendingMachineFSM extends FSM[VendingState, VendingData] {
    //no receive handler
    startWith(Idle, Uninitialised)

    when(Idle) {
      case Event(Initialise(inventory, prices), Uninitialised) =>
        goto(Operational) using Initialised(inventory, prices)
      case _ =>
        sender() ! VendingError("Machine Not Initialised")
        stay()
    }

    when(Operational){
      case Event(RequestProduct(product), Initialised(inventory, prices)) =>
        inventory.get(product) match {
          case None | Some(0) =>
            sender() ! VendingError(s"Product $product is Unavailable")
            stay()
          case _ =>
            goto(WaitForMoney) using WaitForMoneyData(inventory, prices, product, 0, sender())
        }
    }

    when(WaitForMoney, stateTimeout = 1 second){
      case Event(StateTimeout, WaitForMoneyData(inventory, prices, _, amountReceived, requester)) =>
        requester ! VendingError("Money Collection Timed Out")
        if(amountReceived > 0)
          requester ! GiveMoneyBack(amountReceived)
        goto(Operational) using Initialised(inventory, prices)
      case Event(ReceivedMoney(amount), WaitForMoneyData(inventory, prices, product, amountReceived, requester)) =>
        val productPrice = prices(product)
        //When completed price has been paid
        if(amount + amountReceived >= productPrice){
          requester ! Deliver(product)
          val newInventory = inventory + (product -> (inventory(product) - 1))
          if(amount + amountReceived - productPrice > 0)
            requester ! GiveMoneyBack(amountReceived + amount - productPrice)

          goto(Operational) using Initialised(newInventory, prices)
        } else {
          requester ! Instruction(s"Insert ${productPrice - (amountReceived + amount)}")
          stay() using WaitForMoneyData(inventory, prices, product, amountReceived + amount, requester)
        }
    }

    whenUnhandled{
      case Event(_, _) =>
        sender() ! VendingError("Command Not Found")
        stay()
    }

    onTransition{
      case stateA -> stateB => log.info(s"Transitioning From $stateA to $stateB")
    }

    initialize()
  }
}
