/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.plugin

import org.lanternpowered.terre.config.ConfigDirectory
import org.lanternpowered.terre.logger.Logger
import java.lang.String

/**
 * A wrapper to Inject.kt for Java
 * **/
@Suppress("unused")

open class JavaWrapper {
  private val logger = inject<Logger>();
  fun getLogger() : Logger {
    return inject<Logger>()
  }
  fun getConfigDirectory(): ConfigDirectory {
    return inject<ConfigDirectory>()
  }
  fun info(msg: String) {
    val s:kotlin.String =  msg as kotlin.String;
    logger.info(s)
  }
  fun warn(msg: String) {
    val s:kotlin.String =  msg as kotlin.String;
    logger.warn(s)
  }
  fun error(msg: String) {
    val s:kotlin.String =  msg as kotlin.String;
    logger.error(s)
  }
  fun debug(msg: String) {
    val s:kotlin.String =  msg as kotlin.String;
    logger.debug(s)
  }
}
