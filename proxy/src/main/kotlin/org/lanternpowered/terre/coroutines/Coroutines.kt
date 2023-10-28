/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import org.lanternpowered.terre.dispatcher.dispatch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

suspend inline fun <T> withTimeout(
  timeMillis: Long,
  noinline block: suspend CoroutineScope.() -> T
): T {
  return withTimeout(timeMillis, block)
}

suspend fun <T> withTimeout(duration: Duration, block: suspend CoroutineScope.() -> T): T {
  return withTimeout(duration.inWholeMilliseconds, block)
}

/**
 * Continues the current coroutine with the given coroutine dispatcher.
 */
suspend fun continueWith(dispatcher: CoroutineDispatcher) {
  suspendCoroutine { continuation ->
    dispatcher.dispatch {
      continuation.resume(Unit)
    }
  }
}
