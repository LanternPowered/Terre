/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNCHECKED_CAST")

package org.lanternpowered.terre.text

import org.lanternpowered.terre.util.Color

/**
 * Gets a copy of this text object but with the given color, if coloring is supported.
 */
fun <T : Text> T.color(color: Color?): T =
  if (this is ColorableText) color(color) as T else this

/**
 * Represents a text component that can be colored. Doesn't work within localized components.
 */
interface ColorableText : Text {

  /**
   * The color of the text. If null, it will inherit the color of the parent.
   */
  val color: Color?

  /**
   * Gets a copy of this text object but with the given color.
   */
  fun color(color: Color?): ColorableText
}
