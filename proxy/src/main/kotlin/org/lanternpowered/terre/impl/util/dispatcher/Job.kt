/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util.dispatcher

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

internal fun Job.joinBlocking() {
  runBlocking {
    this@joinBlocking.join()
  }
}

internal fun <T> Deferred<T>.awaitBlocking(): T {
  return runBlocking {
    this@awaitBlocking.await()
  }
}
