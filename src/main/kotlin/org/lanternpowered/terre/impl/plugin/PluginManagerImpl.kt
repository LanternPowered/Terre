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

import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.plugin.PluginManager
import java.nio.file.Path

internal class PluginManagerImpl : PluginManager {

  override val all: List<PluginContainer>
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  override fun get(id: String): PluginContainer? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  /**
   * Loads all the plugins from the target directory.
   */
  fun load(directory: Path) {

  }
}
