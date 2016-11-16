package com.typesafe.training.coffeehouse

/**
  * Created by rmukherj on 9/28/16.
  */

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration.FiniteDuration
object Guest {

  case object CoffeeFinished

  def props(waiter: ActorRef, favoriteCoffee: Coffee, finishCoffeeDuration: FiniteDuration): Props =
    Props(new Guest(waiter, favoriteCoffee, finishCoffeeDuration))
}

class Guest(waiter: ActorRef, favoriteCoffee: Coffee, finishCoffeeDuration: FiniteDuration)
  extends Actor with ActorLogging {

  import Guest._
  import context.dispatcher

  private var coffeeCount = 0

  orderFavoriteCoffee()

  override def receive: Receive = {
    case Waiter.CoffeeServed(coffee) =>
      coffeeCount += 1
      log.info("Enjoying my {} yummy {}!", coffeeCount, coffee)
      context.system.scheduler.scheduleOnce(finishCoffeeDuration, self, CoffeeFinished)
    case CoffeeFinished =>
      orderFavoriteCoffee()
  }

  override def postStop(): Unit =
    log.info("Goodbye!")

  private def orderFavoriteCoffee(): Unit =
    waiter ! Waiter.ServeCoffee(favoriteCoffee)
}
