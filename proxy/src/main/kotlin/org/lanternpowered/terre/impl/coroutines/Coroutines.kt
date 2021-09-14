/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal suspend fun <T> tryWithTimeout(
  timeMillis: Long, block: suspend CoroutineScope.() -> T
): Result<T> {
  return try {
    Result.success(withTimeout(timeMillis, block))
  } catch (ex: TimeoutCancellationException) {
    Result.failure(ex)
  }
}

internal suspend fun <T> tryWithTimeout(
  duration: Duration, block: suspend CoroutineScope.() -> T
): Result<T> {
  return tryWithTimeout(duration.inWholeMilliseconds, block)
}
