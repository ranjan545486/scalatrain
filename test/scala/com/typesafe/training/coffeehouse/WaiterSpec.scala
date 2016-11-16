/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.coffeehouse

import akka.testkit.TestProbe

class WaiterSpec extends BaseAkkaSpec {

  "Sending ServeCoffee to Waiter" should {
    "result in sending ApproveCoffee to CoffeeHouse" in {
      val coffeeHouse = TestProbe()
      val guest = TestProbe()
      implicit val _ = guest.ref
      val waiter = system.actorOf(Waiter.props(coffeeHouse.ref))
      waiter ! Waiter.ServeCoffee(Coffee.Akkaccino)
      coffeeHouse.expectMsg(CoffeeHouse.ApproveCoffee(Coffee.Akkaccino, guest.ref))
    }
  }
}
