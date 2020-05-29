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

import org.lanternpowered.terre.impl.ProxyImpl

/**
 * The plugin manager.
 */
interface PluginManager {

  /**
   * A list with all the registered [PluginContainer]s.
   */
  val all: List<PluginContainer>

  /**
   * Attempts to get a [PluginContainer] for the given id.
   */
  operator fun get(id: String): PluginContainer?

  /**
   * The singleton instance of the [PluginManager].
   */
  companion object : PluginManager by ProxyImpl.pluginManager
}
