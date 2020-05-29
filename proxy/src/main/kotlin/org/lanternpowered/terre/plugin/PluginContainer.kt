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

import org.lanternpowered.terre.impl.plugin.activePlugin

/**
 * Represents a plugin.
 */
interface PluginContainer {

  /**
   * The id of the plugin.
   */
  val id: String

  /**
   * The name of the plugin.
   */
  val name: String

  /**
   * The version of the plugin.
   */
  val version: String?

  /**
   * The description of the plugin, if there's one.
   */
  val description: String?

  /**
   * The authors of the plugin.
   */
  val authors: List<String>

  /**
   * The url of the project page of the plugin.
   */
  val url: String?

  /**
   * The instance of the plugin.
   */
  val instance: Any

  companion object {

    /**
     * The current plugin that is executing code.
     */
    val Active: PluginContainer?
      get() = activePlugin
  }
}
