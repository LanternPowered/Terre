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
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.util.ToStringHelper

class TerrePluginContainer(
    override val id: String,
    override val name: String,
    override val version: String? = null,
    override val description: String? = null,
    override val authors: List<String> = listOf(),
    override val url: String? = null,
    override val instance: Any
) : PluginContainer {

  val logger: Logger = LogManager.getLogger(this.id)

  @Deprecated(message = "Prefer the log4j logger.")
  val javaLogger: java.util.logging.Logger by lazy { java.util.logging.Logger.getLogger(this.id) }

  private val toString by lazy {
    ToStringHelper(omitNullValues = true)
        .add("id", this.id)
        .add("name", this.name)
        .add("version", this.version)
        .add("description", this.description)
        .add("authors", this.authors.joinToString(", ", prefix = "[", postfix = "]"))
        .add("url", this.url)
        .add("instance", this.instance)
        .toString()
  }

  override fun toString() = this.toString
}
