/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName", "NOTHING_TO_INLINE", "DEPRECATION")

package org.lanternpowered.terre.util

/**
 * Constructs a new color from the rgb values.
 */
fun Color(red: Byte, green: Byte, blue: Byte) = Color(red.toInt(), green.toInt(), blue.toInt())

/**
 * Constructs a new color from the rgb values. Each component with range 0 - 255
 */
fun Color(red: Int, green: Int, blue: Int) = Color((red shl 16) or (green shl 8) or blue)

/**
 * Represents a color.
 */
inline class Color(val rgb: Int) {

  /**
   * The red component. 0 - 255
   */
  val red: Int
    get() = (this.rgb ushr 16) and 0xff

  /**
   * The green component. 0 - 255
   */
  val green: Int
    get() = (this.rgb ushr 8) and 0xff

  /**
   * The blue component. 0 - 255
   */
  val blue: Int
    get() = this.rgb and 0xff

  override fun toString(): String = "Color(red=$red, green=$green, blue=$blue)"
}

object Colors {

  val Red = Color(255, 0, 0)

  val Lime = Color(0, 255, 0)

  val Green = Color(0, 128, 0)

  val Blue = Color(0, 0, 255)

  val Black = Color(0, 0, 0)

  val Gray = Color(128, 128, 128)

  val Orange = Color(255, 165, 0)

  val Yellow = Color(255, 255, 0)

  val Gold = Color(255, 215, 0)

  val Magenta = Color(255, 0, 255)

  val Purple = Color(128, 0, 128)

  val Cyan = Color(0, 255, 255)

  val White = Color(255, 255, 255)

  val LightGray = Color(211, 211, 211)
}
