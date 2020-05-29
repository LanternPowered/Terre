/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.portals

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.util.ColorHue

/**
 * Represents a portal that is persisted.
 *
 * @property name The name of the portal, used to identify it.
 * @property position The position where the portal is located.
 * @property colorHue The color hue of the portal.
 * @property destination The target server the portal should bring us to.
 */
@Serializable
class PortalData(
    val name: String,
    @SerialName("pos") private val positionArray: FloatArray,
    private val hue: Float,
    val origin: String,
    val destination: String
) {

  val colorHue: ColorHue
    get() = ColorHue(hue)

  val position: Vec2f
    get() = Vec2f(positionArray[0], positionArray[1])

  constructor(name: String, position: Vec2f, hue: ColorHue, origin: String, target: String) :
      this(name, floatArrayOf(position.x, position.y), hue.value, origin, target)
}
