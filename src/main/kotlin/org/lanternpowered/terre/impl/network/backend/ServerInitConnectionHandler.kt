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
import org.lanternpowered.terre.ConnectionRequestResult
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
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

  override fun initialize() {
  }

  override fun disconnect() {
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
    this.future.complete(ConnectionRequestResult.Success(this.connection.server))
    // Sending this packet triggers the client to request all the information
    // from the server once again, this allows it to request and load a new world.
    this.player.clientConnection.send(packet)
    return true
  }

  override fun handleGeneric(packet: Packet) {
    Terre.logger.warn("Received unexpected packet from the backend server: $packet")
  }

  override fun handleUnknown(packet: ByteBuf) {
    Terre.logger.warn("Received unexpected packet from the backend server.")
  }
}
