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
import org.lanternpowered.terre.portal.PortalType
import org.lanternpowered.terre.portal.PortalTypeRegistry

/**
 * Represents a portal that is persisted.
 *
 * @property name The name of the portal, used to identify it.
 * @property position The position where the portal is located.
 * @property destination The target server the portal should bring us to.
 */
@Serializable
class PortalData(
  val name: String,
  @SerialName("type") private val typeName: String,
  @SerialName("pos") private val positionArray: FloatArray,
  val origin: String,
  val destination: String
) {

  val position: Vec2f
    get() = Vec2f(positionArray[0], positionArray[1])

  val type: PortalType
    get() = PortalTypeRegistry[typeName]!!

  constructor(name: String, type: PortalType, position: Vec2f, origin: String, target: String) :
    this(name, type.name, floatArrayOf(position.x, position.y), origin, target)
}
