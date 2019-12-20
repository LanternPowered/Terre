/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.util

import org.lanternpowered.terre.plugin.PluginContainer

/**
 * Constructs a new [NamespacedId] for the given plugin container and id.
 */
fun NamespacedId(plugin: PluginContainer, id: String): NamespacedId
    = NamespacedId(plugin.namespace, id)

/**
 * Represents an id that exists in a specific namespace.
 */
data class NamespacedId(val namespace: Namespace, val id: String) {

  /**
   * Converts this namespaced id to the string format <namespace>:<id>.
   */
  override fun toString(): String = this.namespace.id + ':' + this.id

  companion object {

    /**
     * Parses a namespaced id from the string format <namespace>:<id>.
     */
    fun parse(id: String): NamespacedId {
      val index = id.indexOf(':')
      require(index != -1) { "Invalid namespaced id. $id doesn't match <namespace>:<id>" }
      return NamespacedId(Namespace(id.substring(0, index)), id.substring(index + 1))
    }
  }
}


