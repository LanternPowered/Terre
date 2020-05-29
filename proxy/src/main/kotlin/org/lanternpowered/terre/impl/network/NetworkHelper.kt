/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("DeferredIsResult")

package org.lanternpowered.terre.impl.network

import io.netty.util.concurrent.Future
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.event.EventExecutor
import org.lanternpowered.terre.impl.event.TerreEventBus

@JvmName("toUnitDeferred")
internal fun Future<Void>.toDeferred(): Deferred<Unit> {
  val deferred = CompletableDeferred<Unit>()
  addListener {
    EventExecutor.executor.execute {
      if (it.isSuccess) {
        deferred.complete(Unit)
      } else {
        deferred.completeExceptionally(it.cause())
      }
    }
  }
  return deferred
}

internal fun <V> Future<V>.toDeferred(): Deferred<V> {
  val future = this
  val deferred = CompletableDeferred<V>()
  addListener {
    EventExecutor.executor.execute {
      if (future.isSuccess) {
        deferred.complete(future.get())
      } else {
        deferred.completeExceptionally(future.cause())
      }
    }
  }
  return deferred
}
