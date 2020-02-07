/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.spi.ExtendedLogger
import org.lanternpowered.terre.logger.Logger

internal object Terre {

  /**
   * The name of the implementation.
   */
  val name = ProxyImpl::class.java.`package`.implementationTitle ?: "Terre"

  /**
   * The version of the implementation.
   */
  val version = ProxyImpl::class.java.`package`.implementationVersion ?: ""

  private val backingLogger: ExtendedLogger = LogManager.getLogger(this.name) as ExtendedLogger

  /**
   * The logger of the platform.
   */
  val logger: Logger = LoggerImpl()

  private class LoggerImpl : Logger, ExtendedLogger by backingLogger {

    override fun info(fn: () -> String) {
      if (isInfoEnabled)
        info(fn())
    }

    override fun debug(fn: () -> String) {
      if (isDebugEnabled)
        debug(fn())
    }

    override fun warn(fn: () -> String) {
      if (isWarnEnabled)
        warn(fn())
    }
  }
}
