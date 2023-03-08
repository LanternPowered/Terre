/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.connection

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.event.Event

/**
 * An event that is thrown when a player has logged in successfully. This event is thrown after
 * [PlayerLoginEvent] if the result is not [PlayerLoginEvent.Result.Denied].
 *
 * @property player The player that logged in
 */
data class PlayerPostLoginEvent(
  val player: Player
) : Event
