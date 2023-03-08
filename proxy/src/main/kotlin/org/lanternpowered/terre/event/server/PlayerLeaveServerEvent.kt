/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.server

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.event.Event

/**
 * An event that is thrown when a [Player] leaves the specific [Server]. When switching [Server]s,
 * this event is thrown before the new [PlayerJoinServerEvent].
 *
 * @property player The player that is joining the server.
 * @property server The server the player is joining.
 */
data class PlayerLeaveServerEvent(
  val player: Player,
  val server: Server,
) : Event
