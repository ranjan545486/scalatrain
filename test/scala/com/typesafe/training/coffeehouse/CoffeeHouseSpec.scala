/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.coffeehouse

import akka.actor.ActorDSL._
import akka.testkit.{ EventFilter, TestProbe }
import scala.concurrent.duration.DurationInt

class CoffeeHouseSpec extends BaseAkkaSpec {

  "Creating CoffeeHouse" should {
    "result in logging a status message at debug" in {
      EventFilter.debug(pattern = ".*[Oo]pen.*", occurrences = 1) intercept {
        system.actorOf(CoffeeHouse.props(Int.MaxValue))
      }
    }
    "result in creating a child actor with the name 'barista'" in {
      system.actorOf(CoffeeHouse.props(Int.MaxValue), "create-barista")
      TestProbe().expectActor("/user/create-barista/barista")
    }
    "result in creating a child actor with the name 'waiter'" in {
      system.actorOf(CoffeeHouse.props(Int.MaxValue), "create-waiter")
      TestProbe().expectActor("/user/create-waiter/waiter")
    }
  }

  "Sending CreateGuest to CoffeeHouse" should {
    "result in creating a Guest" in {
      val coffeeHouse = system.actorOf(CoffeeHouse.props(Int.MaxValue), "create-guest")
      coffeeHouse ! CoffeeHouse.CreateGuest(Coffee.Akkaccino, Int.MaxValue)
      TestProbe().expectActor("/user/create-guest/$*")
    }
    "result in logging status guest added to guest book" in {
      val coffeeHouse = system.actorOf(CoffeeHouse.props(Int.MaxValue), "add-to-guest-book")
      EventFilter.info(source = coffeeHouse.path.toString, pattern = ".*added to guest book.*", occurrences = 1) intercept {
        coffeeHouse ! CoffeeHouse.CreateGuest(Coffee.Akkaccino, Int.MaxValue)
      }
    }
  }

  "Sending ApproveCoffee to CoffeeHouse" should {
    "result in forwarding PrepareCoffee to Barista if caffeineLimit not yet reached" in {
      val barista = TestProbe()
      val coffeeHouse =
        actor(new CoffeeHouse(Int.MaxValue) {
          override def createBarista() = barista.ref
        })
      coffeeHouse ! CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, system.deadLetters)
      barista.expectMsg(Barista.PrepareCoffee(Coffee.Akkaccino, system.deadLetters))
    }
    "result in logging status guest caffeine count incremented" in {
      val coffeeHouse = system.actorOf(CoffeeHouse.props(Int.MaxValue), "caffeine-count-incremented-guest-book")
      EventFilter.info(source = coffeeHouse.path.toString, pattern = ".*caffeine count incremented.*", occurrences = 1) intercept {
        val guest = actor(new Act {})
        coffeeHouse ! CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, guest)
      }
    }
    "result in logging a status message at info if caffeineLimit reached" in {
      val coffeeHouse = system.actorOf(CoffeeHouse.props(0))
      EventFilter.info(source = coffeeHouse.path.toString, pattern = ".*[Ss]orry.*", occurrences = 1) intercept {
        val guest = actor(new Act {})
        coffeeHouse ! CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, guest)
      }
    }
    "result in stopping the Guest if caffeineLimit reached" in {
      val probe = TestProbe()
      val guest = actor(new Act {})
      probe.watch(guest)
      val coffeeHouse = system.actorOf(CoffeeHouse.props(0))
      coffeeHouse ! CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, guest)
      probe.expectTerminated(guest)
    }
  }

  "On termination of Guest, CoffeeHouse" should {
    "remove the guest from the guest book" in {
      val barista = TestProbe()
      val coffeeHouse =
        actor(new CoffeeHouse(Int.MaxValue) {
          override def createBarista() = barista.ref
        })
      coffeeHouse ! CoffeeHouse.CreateGuest(Coffee.Akkaccino, Int.MaxValue)
      val guest = barista.expectMsgPF() {
        case Barista.PrepareCoffee(Coffee.Akkaccino, guest) => guest
      }
      barista.watch(guest)
      system.stop(guest)
      barista.expectTerminated(guest)
      barista.within(2 seconds) {
        barista.awaitAssert {
          coffeeHouse ! CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, guest)
          barista.expectMsgPF(100 milliseconds) {
            case Barista.PrepareCoffee(Coffee.Akkaccino, `guest`) => ()
          }
        }
      }
    }
    "result in logging a thanks message at info" in {
      val coffeeHouse = system.actorOf(CoffeeHouse.props(1), "thanks-coffee-house")
      EventFilter.info(source = coffeeHouse.path.toString, pattern = ".*for being our guest.*", occurrences = 1) intercept {
        coffeeHouse ! CoffeeHouse.CreateGuest(Coffee.Akkaccino, Int.MaxValue)
        val guest = TestProbe().expectActor("/user/thanks-coffee-house/$*")
        coffeeHouse ! CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, guest)
      }
    }
  }
}
