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

import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.delay

suspend fun <T> withTimeout(duration: Duration, block: suspend CoroutineScope.() -> T): T {
  return withTimeout(duration.toLongMilliseconds(), block)
}

suspend fun delay(duration: Duration) {
  delay(duration.toLongMilliseconds())
}
