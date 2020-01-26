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

/**
 * Represents a collection of players.
 */
interface PlayerCollection : Collection<Player> {

  /**
   * Attempts to get the player for the given [PlayerIdentifier].
   */
  operator fun get(identifier: PlayerIdentifier): Player?
}
