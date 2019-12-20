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

import java.util.concurrent.ExecutorService as JavaExecutorService
import java.util.concurrent.ScheduledExecutorService as JavaScheduledExecutorService

fun JavaExecutorService.asCompletableExecutorService(): ExecutorService {
  if (this is ExecutorService) {
    return this
  }
  if (this is JavaScheduledExecutorService) {
    return WrappedScheduledExecutorService(this)
  }
  return WrappedExecutorService(this)
}

fun JavaScheduledExecutorService.asCompletableExecutorService(): ScheduledExecutorService {
  if (this is ScheduledExecutorService) {
    return this
  }
  return WrappedScheduledExecutorService(this)
}
