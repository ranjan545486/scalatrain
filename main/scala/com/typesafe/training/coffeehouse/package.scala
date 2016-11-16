/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

package object coffeehouse {

  type Traversable[+A] = scala.collection.immutable.Traversable[A]

  type Iterable[+A] = scala.collection.immutable.Iterable[A]

  type Seq[+A] = scala.collection.immutable.Seq[A]

  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  /**
   * Keeps the CPU busy for the given appoximate duration.
   */
  def busy(duration: FiniteDuration): Unit =
    pi(System.nanoTime() + duration.toNanos)

  /**
   * Calculate pi until System.nanoTime is later than `endNanos`
   */
  private def pi(endNanos: Long) = {
    def gregoryLeibnitz(n: Long) = 4.0 * (1 - (n % 2) * 2) / (n * 2 + 1)
    var n = 0
    var acc = BigDecimal(0.0)
    while (System.nanoTime() < endNanos) {
      acc += gregoryLeibnitz(n)
      n += 1
    }
    acc
  }
}
