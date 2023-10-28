/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.player

import org.lanternpowered.terre.Team
import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.impl.buildNamedCatalogTypeRegistryOf
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.Colors

internal data class TeamImpl(
  override val name: String,
  override val numericId: Int,
  override val color: Color,
) : Team, NumericCatalogType {

  companion object {
    val None = TeamImpl("none", 0, Colors.White)
    val Red = TeamImpl("red", 1, Color(218, 59, 59))
    val Green = TeamImpl("green", 2, Color(59, 218, 85))
    val Blue = TeamImpl("blue", 3, Color(59, 149, 218))
    val Yellow = TeamImpl("yellow", 4, Color(242, 221, 100))
    val Pink = TeamImpl("pink", 5, Color(224, 100, 242))
  }
}

internal val TeamRegistryImpl = buildNamedCatalogTypeRegistryOf<Team>({ null }) {
  register(TeamImpl.None)
  register(TeamImpl.Red)
  register(TeamImpl.Green)
  register(TeamImpl.Blue)
  register(TeamImpl.Yellow)
  register(TeamImpl.Pink)
}
