/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.logger

import org.apache.logging.log4j.spi.ExtendedLogger
import org.apache.logging.log4j.Logger as Log4jLogger
import org.lanternpowered.terre.logger.Logger

internal class LoggerImpl(logger: Log4jLogger) :
  Logger, ExtendedLogger by (logger as ExtendedLogger) {

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
