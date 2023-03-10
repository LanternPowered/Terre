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
import java.util.UUID

/**
 * An event that is thrown when a [Player] joins or switches to a specific [Server].
 *
 * @property player The player that is joining the server.
 * @property server The server the player is joining.
 * @property clientUniqueId The unique id that server will know the [player] as.
 */
data class PlayerJoinServerEvent(
  val player: Player,
  val server: Server,
  var clientUniqueId: UUID = player.clientUniqueId,
) : Event
