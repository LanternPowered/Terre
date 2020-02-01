/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.text

interface TextSerializer {

  /**
   * Serializes the [Text] object into a string.
   */
  fun serialize(text: Text): String

  /**
   * Deserializes the [Text] object from a string.
   */
  fun deserialize(string: String): Text
}
