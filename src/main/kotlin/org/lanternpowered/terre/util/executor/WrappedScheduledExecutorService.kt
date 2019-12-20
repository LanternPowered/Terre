/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.util.executor

import java.util.concurrent.Callable
import java.util.concurrent.Delayed
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledExecutorService as JavaScheduledExecutorService

internal class WrappedScheduledExecutorService(
    private val executor: JavaScheduledExecutorService
) : WrappedExecutorService(executor), ScheduledExecutorService {

  override fun <V> schedule(
      callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledCompletableFuture<V> {
    val completableFuture = ScheduledCompletableFutureImpl<V>()
    completableFuture.scheduledFuture = this.executor.schedule({
      try {
        completableFuture.complete(callable.call())
      } catch (e: Throwable) {
        completableFuture.completeExceptionally(e)
      }
    }, delay, unit)
    return completableFuture
  }

  override fun schedule(
      command: Runnable, delay: Long, unit: TimeUnit): ScheduledCompletableFuture<Unit> {
    val completableFuture = ScheduledCompletableFutureImpl<Unit>()
    completableFuture.scheduledFuture = this.executor.schedule({
      try {
        command.run()
        completableFuture.complete(Unit)
      } catch (e: Throwable) {
        completableFuture.completeExceptionally(e)
      }
    }, delay, unit)
    return completableFuture
  }

  override fun scheduleAtFixedRate(
      command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledCompletableFuture<Unit> {
    val completableFuture = ScheduledCompletableFutureImpl<Unit>()
    completableFuture.scheduledFuture = this.executor.scheduleAtFixedRate({
      try {
        command.run()
        completableFuture.complete(Unit)
      } catch (e: Throwable) {
        completableFuture.completeExceptionally(e)
      }
    }, initialDelay, period, unit)
    return completableFuture
  }

  override fun scheduleWithFixedDelay(
      command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledCompletableFuture<Unit> {
    val completableFuture = ScheduledCompletableFutureImpl<Unit>()
    completableFuture.scheduledFuture = this.executor.scheduleWithFixedDelay({
      try {
        command.run()
        completableFuture.complete(Unit)
      } catch (e: Throwable) {
        completableFuture.completeExceptionally(e)
      }
    }, initialDelay, delay, unit)
    return completableFuture
  }
}

private class ScheduledCompletableFutureImpl<T> : ScheduledCompletableFuture<T>() {

  lateinit var scheduledFuture: ScheduledFuture<*>

  override fun compareTo(other: Delayed): Int = this.scheduledFuture.compareTo(other)

  override fun getDelay(unit: TimeUnit): Long = this.scheduledFuture.getDelay(unit)
}
