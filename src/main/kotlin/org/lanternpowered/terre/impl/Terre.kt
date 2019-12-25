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
import org.apache.logging.log4j.Logger

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
   * The logger of the platform.
   */
  val logger: Logger = LogManager.getLogger(Terre)
}
