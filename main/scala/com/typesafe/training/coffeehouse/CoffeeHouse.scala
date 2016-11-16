package com.typesafe.training.coffeehouse

import java.nio.file.attribute.FileAttributeView

import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated }

import scala.concurrent.duration.{Duration, MILLISECONDS => Millis}

/**
  * Created by rmukherj on 9/28/16.
  */


class CoffeeHouse(caffeineLimit: Int) extends Actor with ActorLogging {

  import CoffeeHouse._

  private val baristaPrepareCoffeeDuration =
    Duration(context.system.settings.config.getDuration(
      "coffee-house.barista.prepare-coffee-duration", Millis), Millis)
  private val guestFinishCoffeeDuration =
    Duration(context.system.settings.config.getDuration(
      "coffee-house.guest.finish-coffee-duration", Millis), Millis)

  private val barista = createBarista()
  private val waiter = createWaiter()

  private var guestBook = Map.empty[ActorRef, Int] withDefaultValue 0

  log.debug("CoffeeHouse Open")

  override def receive: Receive = {
    case CreateGuest(favoriteCoffee) =>
      val guest: ActorRef = createGuest(favoriteCoffee)
      guestBook += guest -> (guestBook(guest) + 0)
      log.info(s"Guest $guest added to guest book.")
      context.watch(guest)
    case ApproveCoffee(coffee, guest) if guestBook(guest) < caffeineLimit =>
      guestBook += guest -> (guestBook(guest) + 1)
      log.info(s"Guest $guest caffeine count incremented.")
      barista forward Barista.PrepareCoffee(coffee, guest)
    case ApproveCoffee(coffee, guest) =>
      log.info(s"Sorry, $guest, but you have reached your limit.")
      context.stop(guest)
    case Terminated(guest) =>
      log.info(s"Thanks, $guest, for being our guest!")
      guestBook -= guest

  }

  protected def createBarista(): ActorRef =
    context.actorOf(Barista.props(baristaPrepareCoffeeDuration), "barista")

  protected def createWaiter(): ActorRef =
    context.actorOf(Waiter.props(self), "waiter")

  protected def createGuest(favoriteCoffee: Coffee): ActorRef = {
    context.actorOf(Guest.props(waiter, favoriteCoffee, guestFinishCoffeeDuration))
  }

}

object CoffeeHouse {

  case class CreateGuest(favoriteCoffee: Coffee)
  case class ApproveCoffee(coffee: Coffee, guest: ActorRef)

  def props(caffeineLimit: Int): Props =
    Props(new CoffeeHouse(caffeineLimit))
}