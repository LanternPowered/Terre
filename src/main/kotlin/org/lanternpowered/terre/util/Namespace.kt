/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.util

/**
 * Represents a namespace.
 */
inline class Namespace(val id: String) {

  /**
   * Creates a new [NamespacedId] within this namespace.
   */
  fun id(id: String): NamespacedId = NamespacedId(this, id)

  companion object {

    /**
     * The terre namespace.
     */
    val Terre = Namespace("terre")
  }
}
