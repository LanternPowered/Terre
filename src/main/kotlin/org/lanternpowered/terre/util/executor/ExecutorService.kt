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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.ExecutorService as JavaExecutorService

interface ExecutorService : JavaExecutorService {

  override fun execute(command: Runnable)

  override fun submit(task: Runnable): CompletableFuture<Unit>

  override fun <T> submit(task: Runnable, result: T): CompletableFuture<T>

  override fun <T> submit(task: Callable<T>): CompletableFuture<T>

  override fun shutdownNow(): List<Runnable>

  override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>>

  override fun <T> invokeAll(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): List<Future<T>>

  override fun <T> invokeAny(tasks: Collection<Callable<T>>): T

  override fun <T> invokeAny(tasks: Collection<Callable<T>>, timeout: Long, unit: TimeUnit): T

  override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean
}
