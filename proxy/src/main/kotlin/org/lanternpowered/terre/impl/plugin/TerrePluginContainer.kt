/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.plugin

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.config.ConfigDirectoryImpl
import org.lanternpowered.terre.impl.logger.LoggerImpl
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.util.toString

internal class TerrePluginContainer(
  override val id: String,
  override val name: String = id,
  override val version: String? = null,
  override val description: String? = null,
  override val authors: List<String> = listOf(),
  override val url: String? = null,
  override val instance: Any
) : PluginContainer {

  val logger: Logger = LoggerImpl(LogManager.getLogger(this.id))

  @Deprecated(message = "Prefer the log4j logger.")
  val javaLogger: java.util.logging.Logger by lazy { java.util.logging.Logger.getLogger(this.id) }

  val configDirectory = ConfigDirectoryImpl(ProxyImpl.configDirectory.path.resolve(this.id))

  private val toString by lazy {
    toString(name = "PluginContainer", omitNullValues = true) {
      "id" to id
      "name" to name
      "version" to version
      "description" to description
      "authors" to authors.joinToString(", ", prefix = "[", postfix = "]")
      "url" to url
      "instance" to instance
    }
  }

  override fun toString() = this.toString
}
