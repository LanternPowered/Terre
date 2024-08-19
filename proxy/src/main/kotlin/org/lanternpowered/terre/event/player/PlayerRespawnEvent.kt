/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.player

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.event.Event

/**
 * An event that is thrown when a player respawns.
 */
data class PlayerRespawnEvent(
  val player: Player,
) : Event
