/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util

import org.lanternpowered.terre.util.collection.toImmutableList
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Combines the result of the multiple futures into a single future.
 */
internal fun <V> listFutureOf(iterable: Iterable<Future<V>>): Future<List<V>> {
  val futures = iterable.toImmutableList()
  return object : Future<List<V>> {

    override fun isDone(): Boolean  = futures.all { future -> future.isDone }

    override fun get(): List<V> {
      val list = mutableListOf<V>()
      for (future in futures) {
        list += future.get()
      }
      return list
    }

    override fun get(timeout: Long, unit: TimeUnit): List<V> {
      val endTime = System.nanoTime() + unit.toNanos(timeout)
      val list = mutableListOf<V>()
      for (future in futures) {
        list += future.get(max(0, endTime - System.nanoTime()), TimeUnit.NANOSECONDS)
      }
      return list
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
      var success = true
      for (future in futures) {
        success = success && future.cancel(mayInterruptIfRunning)
      }
      return success
    }

    override fun isCancelled(): Boolean = futures.all { future -> future.isCancelled }
  }
}
