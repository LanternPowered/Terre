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

import kotlinx.coroutines.Deferred
import org.lanternpowered.terre.text.MessageReceiver
import org.lanternpowered.terre.text.Text

/**
 * Represents a player.
 */
interface Player : Named, MessageReceiver, InboundConnection {

  /**
   * The identifier of this player.
   */
  val identifier: PlayerIdentifier

  /**
   * The latency of the connection to the client.
   */
  val latency: Int

  /**
   * The connection to a server, if present.
   */
  val serverConnection: ServerConnection?

  /**
   * Disconnects the player with the specified reason.
   */
  suspend fun disconnect(reason: Text) = disconnectAsync(reason).await()

  /**
   * Disconnects the player with the specified reason.
   */
  fun disconnectAsync(reason: Text): Deferred<Unit>
}
