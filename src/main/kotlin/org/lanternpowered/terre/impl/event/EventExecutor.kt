/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.event

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.asCoroutineDispatcher
import org.lanternpowered.terre.impl.util.TerreThread
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object EventExecutor {

  val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
      ThreadFactoryBuilder()
          .setNameFormat("event-executor-#%d")
          .setDaemon(true)
          .setThreadFactory(::TerreThread)
          .build())

  val dispatcher = this.executor.asCoroutineDispatcher()
}
