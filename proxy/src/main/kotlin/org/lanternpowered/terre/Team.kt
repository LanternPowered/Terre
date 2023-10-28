/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre

import org.lanternpowered.terre.catalog.NamedCatalogType
import org.lanternpowered.terre.catalog.NamedCatalogTypeRegistry
import org.lanternpowered.terre.impl.player.TeamImpl
import org.lanternpowered.terre.impl.player.TeamRegistryImpl
import org.lanternpowered.terre.util.Color

interface Team : NamedCatalogType {

  val color: Color

  companion object {

    /**
     * No team.
     */
    val None: Team = TeamImpl.None

    /**
     * The red team.
     */
    val Red: Team = TeamImpl.Red

    /**
     * The green team.
     */
    val Green: Team = TeamImpl.Green

    /**
     * The blue team.
     */
    val Blue: Team = TeamImpl.Blue

    /**
     * The yellow team.
     */
    val Yellow: Team = TeamImpl.Yellow

    /**
     * The pink team.
     */
    val Pink: Team = TeamImpl.Pink
  }
}

/**
 * A registry for all the [Team]s.
 */
object TeamRegistry : NamedCatalogTypeRegistry<Team> by TeamRegistryImpl
