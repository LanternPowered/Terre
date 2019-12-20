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
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledExecutorService as JavaScheduledExecutorService

interface ScheduledExecutorService : ExecutorService, JavaScheduledExecutorService {

  override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledCompletableFuture<V>

  override fun schedule(
      command: Runnable, delay: Long, unit: TimeUnit): ScheduledCompletableFuture<Unit>

  override fun scheduleAtFixedRate(
      command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledCompletableFuture<Unit>

  override fun scheduleWithFixedDelay(
      command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledCompletableFuture<Unit>
}
