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
import org.lanternpowered.terre.Team
import org.lanternpowered.terre.event.Event

/**
 * An even that is thrown when a [Player] tries to switch to the specified [Team]. Can be
 * cancelled to prevent a player from switching. The backing server can still prevent the player
 * from switching even if this event is not cancelled.
 */
data class PlayerChangeTeamEvent(
  val player: Player,
  val team: Team,
  var cancelled: Boolean = false,
) : Event
