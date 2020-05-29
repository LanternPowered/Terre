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
import kotlinx.coroutines.Job
import org.lanternpowered.terre.portal.PortalAware
import org.lanternpowered.terre.text.MessageReceiver
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf

/**
 * Represents a player.
 */
interface Player : Named, MessageReceiver, MessageSender, PortalAware, InboundConnection {

  /**
   * Whether this player connected using a mobile client.
   */
  val isMobile: Boolean

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
  suspend fun disconnect(reason: Text = DefaultDisconnectReason) = disconnectAsync(reason).join()

  /**
   * Disconnects the player with the specified reason.
   */
  fun disconnectAsync(reason: Text = DefaultDisconnectReason): Job

  /**
   * Attempts to connect to the given [Server]. This will switch from the current
   * server if the player is already connected to one.
   *
   * This doesn't disconnect the player from the current server if the connection
   * attempts failed.
   *
   * @param server The server to connect to
   * @return The connection request result
   */
  suspend fun connectTo(server: Server) = connectToAsync(server).await()

  /**
   * Attempts to connect to the given [Server] async. This will switch from the current
   * server if the player is already connected to one. This doesn't disconnect the player
   * from the current server if the connection attempts failed.
   *
   * This doesn't disconnect the player from the current server if the connection
   * attempts failed.
   *
   * @param server The server to connect to
   * @return The connection request result
   */
  fun connectToAsync(server: Server): Deferred<ServerConnectionRequestResult>

  /**
   * Attempts to connect to one of the given [Server]s.
   *
   * This doesn't disconnect the player from the current server if the
   * connection attempts failed.
   *
   * @return The server that a connection was made to, if successful
   */
  fun connectToAnyAsync(first: Server, second: Server, vararg more: Server)
      = connectToAnyAsync(listOf(first, second) + more.asList())

  /**
   * Attempts to connect to one of the given [Server]s.
   *
   * This doesn't disconnect the player from the current server if the
   * connection attempts failed.
   *
   * @return The server that a connection was made to, if successful
   */
  fun connectToAnyAsync(servers: Iterable<Server>): Deferred<Server?>

  /**
   * Attempts to connect to one of the given [Server]s.
   *
   * This doesn't disconnect the player from the current server if the
   * connection attempts failed.
   *
   * @return The server that a connection was made to, if successful
   */
  suspend fun connectToAny(first: Server, second: Server, vararg more: Server)
      = connectToAny(listOf(first, second) + more.asList())

  /**
   * Attempts to connect to one of the given [Server]s.
   *
   * This doesn't disconnect the player from the current server if the
   * connection attempts failed.
   *
   * @return The server that a connection was made to, if successful
   */
  suspend fun connectToAny(servers: Iterable<Server>)
      = connectToAnyAsync(servers).await()
}

/**
 * The default disconnect reason.
 */
val DefaultDisconnectReason = textOf("You were disconnected.")
