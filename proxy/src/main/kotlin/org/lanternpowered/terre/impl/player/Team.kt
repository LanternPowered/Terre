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

import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.impl.buildNumericCatalogTypeRegistryOf
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.Colors

internal data class Team(
  override val numericId: Int,
  val color: Color
) : NumericCatalogType {

  companion object {
    val White = Team(0, Colors.White)
    val Red = Team(1, Color(218, 59, 59))
    val Green = Team(2, Color(59, 218, 85))
    val Blue = Team(3, Color(59, 149, 218))
    val Yellow = Team(4, Color(242, 221, 100))
    val Purple = Team(5, Color(224, 100, 242))
  }
}

internal val TeamRegistry = buildNumericCatalogTypeRegistryOf<Team>({ null }) {
  register(Team.White)
  register(Team.Red)
  register(Team.Green)
  register(Team.Blue)
  register(Team.Yellow)
  register(Team.Purple)
}
