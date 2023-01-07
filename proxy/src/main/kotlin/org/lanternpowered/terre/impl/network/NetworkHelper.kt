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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal suspend fun <T> Future<T>.join(): T {
  if (isDone) {
    val cause = cause()
    if (cause != null) {
      throw cause
    }
    return get()
  }
  return suspendCoroutine { continuation ->
    addListener { future ->
      val cause = future.cause()
      if (cause != null) {
        continuation.resumeWithException(cause)
      } else {
        @Suppress("UNCHECKED_CAST")
        continuation.resume(future.get() as T)
      }
    }
  }
}
