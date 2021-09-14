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

/**
 * Something that can be represented as [Text].
 */
interface TextLike {

  /**
   * Converts this text like to [Text].
   */
  fun text(): Text
}
