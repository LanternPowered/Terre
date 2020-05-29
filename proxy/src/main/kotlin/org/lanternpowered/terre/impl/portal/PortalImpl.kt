/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.portal

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.util.ColorHue
import java.util.UUID
import kotlin.math.abs

class PortalImpl(
    override val id: UUID,
    override val server: Server,
    override val position: Vec2f,
    override val colorHue: ColorHue
) : Portal {

  override fun onUse(onUse: suspend Portal.(player: Player) -> Unit) {
    TODO("Not yet implemented")
  }

  override fun close() {
    TODO("Not yet implemented")
  }

  companion object {

    /**
     * The number of possible base ids. This also the
     * maximum number of different portal colors.
     */
    const val BASE_ID_COUNT = 25

    private val HUE_VALUES = FloatArray(BASE_ID_COUNT)

    init {
      // A table with all the possible hue values for portals
      for (i in HUE_VALUES.indices)
        HUE_VALUES[i] = (i.toFloat() * 0.08f + 0.5f) % 1.0f
    }

    /**
     * Generates the base id for a portal based on the color hue.
     */
    fun generateBaseId(colorHue: ColorHue): Int {
      // Find the closest hue value and return its index
      return HUE_VALUES
          .mapIndexed { index, hue -> index to abs(colorHue.value - hue) }
          .minBy { (_, hueDiff) -> hueDiff }!! // Find the closest hue value
          .first
    }
  }
}
