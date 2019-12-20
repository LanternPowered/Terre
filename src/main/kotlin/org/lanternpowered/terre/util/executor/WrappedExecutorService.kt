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
import java.util.concurrent.ExecutorService as JavaExecutorService

internal open class WrappedExecutorService(
    private val executor: JavaExecutorService
) : ExecutorService, JavaExecutorService by executor {

  override fun submit(task: Runnable): CompletableFuture<Unit> {
    val completableFuture = CompletableFuture<Unit>()
    this.executor.execute {
      try {
        task.run()
        completableFuture.complete(Unit)
      } catch (t: Throwable) {
        completableFuture.completeExceptionally(t)
      }
    }
    return completableFuture
  }

  override fun <T> submit(task: Runnable, result: T): CompletableFuture<T> {
    val completableFuture = CompletableFuture<T>()
    this.executor.execute {
      try {
        task.run()
        completableFuture.complete(result)
      } catch (t: Throwable) {
        completableFuture.completeExceptionally(t)
      }
    }
    return completableFuture
  }

  override fun <T> submit(task: Callable<T>): CompletableFuture<T> {
    val completableFuture = CompletableFuture<T>()
    this.executor.execute {
      try {
        completableFuture.complete(task.call())
      } catch (t: Throwable) {
        completableFuture.completeExceptionally(t)
      }
    }
    return completableFuture
  }
}
