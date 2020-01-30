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
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.impl.player.ServerConnectionImpl
import java.util.concurrent.CompletableFuture

/**
 * The initial connection handler that is used
 * to establish a connection to a server.
 */
internal class ServerInitConnectionHandler(
    val player: PlayerImpl,
    val connection: ServerConnectionImpl,
    val future: CompletableFuture<ConnectionRequestResult>
) : ConnectionHandler {

  private var playerId: PlayerId? = null

  companion object {

    private val notFirstServerConnection: AttributeKey<Boolean>
        = AttributeKey.valueOf("not-first-server-connection")
  }

  override fun initialize() {
  }

  override fun disconnect() {
    if (!this.future.isDone)
      this.future.complete(ConnectionRequestResult.Disconnected(this.connection.server, null))
  }

  override fun handle(packet: DisconnectPacket): Boolean {
    this.future.complete(ConnectionRequestResult.Disconnected(this.connection.server, packet.reason))
    return true
  }

  override fun handle(packet: PasswordRequestPacket): Boolean {
    this.connection.ensureConnected().send(
        PasswordResponsePacket(this.connection.server.info.password ?: ""))
    return true
  }

  override fun handle(packet: ConnectionApprovedPacket): Boolean {
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
