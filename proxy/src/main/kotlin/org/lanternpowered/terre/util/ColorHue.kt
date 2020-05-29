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
 * Represents the hue value of a color. Ranges from 0.0 to 1.0.
 */
inline class ColorHue(val value: Float)

object ColorHues {

  val Red = ColorHue(0f)

  val Orange = ColorHue(0.0833f)

  val Yellow = ColorHue(0.167f)

  val Green = ColorHue(0.333f)

  val Cyan = ColorHue(0.5f)

  val Blue = ColorHue(0.667f)

  val Purple = ColorHue(0.833f)
}
