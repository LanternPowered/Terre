/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.backend

import io.netty.buffer.ByteBuf
import io.netty.util.AttributeKey
import org.lanternpowered.terre.ConnectionRequestResult
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.math.Vec2i
import org.lanternpowered.terre.impl.network.ClientVersion
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.player.ServerConnectionImpl
import org.lanternpowered.terre.text.Text
import java.util.concurrent.CompletableFuture

/**
 * The initial connection handler that is used
 * to establish a connection to a server.
 *
 * @property connection The server connection
 * @property future The future that will be notified of the connection result
 * @param versionsToAttempt The versions that should be attempted to use to handshake with the server
 */
internal class ServerInitConnectionHandler(
    private val connection: ServerConnectionImpl,
    private val future: CompletableFuture<ConnectionRequestResult>,
    versionsToAttempt: List<ClientVersion>
) : ConnectionHandler {

  private val player = this.connection.player
  private var playerId: PlayerId? = null

  private lateinit var attemptedVersion: ClientVersion
  private val versionsToAttemptQueue = versionsToAttempt.toMutableList()
  private var firstDisconnectReason: Text? = null

  init {
    check(versionsToAttempt.isNotEmpty())
  }

  companion object {

    private val notFirstServerConnection: AttributeKey<Boolean>
        = AttributeKey.valueOf("not-first-server-connection")
  }

  override fun initialize() {
    // Prioritize the last known client version if present, it's more likely that
    // the client will be allowed using this version.
    val lastKnownVersion = this.connection.server.lastKnownVersion
    if (lastKnownVersion != null && lastKnownVersion in this.versionsToAttemptQueue
        && this.versionsToAttemptQueue.size > 1) {
      this.versionsToAttemptQueue -= lastKnownVersion
      this.versionsToAttemptQueue.add(0, lastKnownVersion)
    }
    // Try initial handshake
    tryNextHandshake()
  }

  private fun tryNextHandshake() {
    if (this.versionsToAttemptQueue.isEmpty()) {
      // No more versions to try, we are done
      this.future.complete(ConnectionRequestResult.Disconnected(this.connection.server, this.firstDisconnectReason))
    } else {
      // Try the next version
      val version = this.versionsToAttemptQueue.removeAt(0)
      this.connection.ensureConnected().send(ConnectionRequestPacket(version))
      this.attemptedVersion = version
    }
  }

  override fun disconnect() {
    if (!this.future.isDone)
      this.future.complete(ConnectionRequestResult.Disconnected(this.connection.server, null))
  }

  override fun handle(packet: DisconnectPacket): Boolean {
    if (this.future.isDone)
      return true
    if (this.firstDisconnectReason == null)
      this.firstDisconnectReason = packet.reason
    // Disconnect doesn't mean the end, try again with
    // another client version.
    tryNextHandshake()
    return true
  }

  override fun handle(packet: PasswordRequestPacket): Boolean {
    this.connection.ensureConnected().send(
        PasswordResponsePacket(this.connection.server.info.password ?: ""))
    return true
  }

  override fun handle(packet: ConnectionApprovedPacket): Boolean {
    // Connection was approved so the client version was accepted
    this.connection.server.lastKnownVersion = this.attemptedVersion
    this.playerId = packet.playerId
    // Sending this packet triggers the client to request all the information
    // from the server once again, this allows it to request and load a new world.
    this.player.clientConnection.send(packet)
    return true
  }

  override fun handle(packet: CompleteConnectionPacket): Boolean {
    val playerId = this.playerId ?: error("Player id isn't known.")

    val notFirstConnection = this.player.clientConnection.attr(notFirstServerConnection).getAndSet(true)
    if (notFirstConnection) {
      // Sending this packet makes sure that the player spawns, even if the client
      // was previously connected to another world. This will trigger the client
      // to find a new spawn location.
      this.player.clientConnection.send(PlayerSpawnPacket(playerId, Vec2i.Zero))
    } else {
      // Notify the client that the connection is complete, this will attempt
      // to spawn the player in the world, only affects the first time the client
      // connects to a server.
      this.player.clientConnection.send(packet)
    }

    // Connection phase is complete.
    this.future.complete(ConnectionRequestResult.Success(this.connection.server))
    return true
  }

  override fun handleGeneric(packet: Packet) {
    Terre.logger.warn("Received unexpected packet from the backend server: $packet")
  }

  override fun handleUnknown(packet: ByteBuf) {
    Terre.logger.warn("Received unexpected packet from the backend server.")
  }
}
