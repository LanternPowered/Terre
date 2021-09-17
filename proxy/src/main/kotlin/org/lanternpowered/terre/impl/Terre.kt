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
import org.lanternpowered.terre.impl.logger.LoggerImpl
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.util.Color

internal object Terre {

  /**
   * The name of the implementation.
   */
  val name = ProxyImpl::class.java.`package`.implementationTitle ?: "Terre"

  /**
   * The version of the implementation.
   */
  val version = ProxyImpl::class.java.`package`.implementationVersion ?: ""

  /**
   * The color used for chat messages from terre.
   */
  val color = Color(61, 105, 95)

  /**
   * The prefix used in from of terre messages.
   */
  private val messagePrefix = "[$name] ".text().color(color)

  fun message(text: Text) = messagePrefix + text

  fun message(text: String) = message(text.text())

  /**
   * The logger of the platform.
   */
  val logger: Logger = LoggerImpl(LogManager.getLogger(name))
}
