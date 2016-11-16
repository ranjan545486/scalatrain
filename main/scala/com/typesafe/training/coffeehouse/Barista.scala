package com.typesafe.training.coffeehouse

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}

import scala.concurrent.duration.FiniteDuration

/**
  * Created by rmukherj on 9/29/16.
  */
class Barista (prepareCoffeeDuration : FiniteDuration) extends Actor {

  import Barista._

  override def receive: Receive = {
    case PrepareCoffee(coffee, guest) =>
      busy(prepareCoffeeDuration)
      sender() ! CoffeePrepared(coffee, guest)
  }

}

object Barista {

  case class PrepareCoffee(coffee: Coffee, guest: ActorRef)
  case class CoffeePrepared(coffee: Coffee, guest: ActorRef)

  def props(prepareCoffeeDuration: FiniteDuration): Props =
    Props(new Barista(prepareCoffeeDuration))
}




