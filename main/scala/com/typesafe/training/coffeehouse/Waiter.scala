package com.typesafe.training.coffeehouse

import akka.actor.Actor.Receive
import akka.actor.{Actor,ActorRef, Props}


/**
  * Created by rmukherj on 9/28/16.
  */
object Waiter {

  case class ServeCoffee(coffee: Coffee)
  case class CoffeeServed(coffee: Coffee)

  def props(coffeeHouse: ActorRef): Props =
    Props(new Waiter(coffeeHouse))
}

class Waiter(coffeeHouse: ActorRef) extends Actor {

  import Waiter._

  override def receive: Receive = {
    case ServeCoffee(coffee) => coffeeHouse ! CoffeeHouse.ApproveCoffee(coffee, sender())
    case Barista.CoffeePrepared(coffee, guest) => guest ! CoffeeServed(coffee)
  }
}