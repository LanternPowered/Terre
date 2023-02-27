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
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.MultistateProtocol
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import java.util.concurrent.CompletableFuture

/**
 * The initial connection handler that is used
 * to establish a connection to a server.
 *
 * @property connection The server connection
 * @property future The future that will be notified of the connection result
 * @property version The protocol version to use to handshake
 * @property protocol The protocol to use
 */
internal class ServerInitConnectionHandler(
  private val connection: Connection,
  private val clientConnection: Connection,
  private val future: CompletableFuture<ServerInitConnectionResult>,
  private val version: ProtocolVersion,
  private val protocol: MultistateProtocol,
  private val password: String,
  private val playerInfo: PlayerInfoPacket,
) : ConnectionHandler {

  private var playerId: PlayerId? = null
  private var accepted: Boolean = false

  override fun initialize() {
    connection.protocol = ServerInitProtocol
    connection.send(ConnectionRequestPacket(version))
    debug { "Send server connection request to ${connection.remoteAddress} with $version" }
  }

  override fun disconnect() {
    future.complete(ServerInitConnectionResult.Disconnected(null))
  }

  override fun exception(throwable: Throwable) {
    future.completeExceptionally(throwable)
    connection.close()
  }

  override fun handle(packet: DisconnectPacket): Boolean {
    val result = if (accepted) {
      ServerInitConnectionResult.Disconnected(packet.reason)
    } else {
      ServerInitConnectionResult.UnsupportedProtocol(packet.reason)
    }
    future.complete(result)
    // Make sure that the connection gets closed
    connection.close()
    debug { "Disconnect: ${packet.reason}" }
    return true
  }

  override fun handle(packet: PasswordRequestPacket): Boolean {
    connection.send(PasswordResponsePacket(password))
    debug { "Password request -> response: $password" }
    return true
  }

  override fun handle(packet: ConnectionApprovedPacket): Boolean {
    val playerId = packet.playerId
    debug { "Connection approved: $playerId" }
    accepted = true
    this.playerId = playerId
    // Send player info, the server responds either with player info if ok, or disconnects
    connection.send(playerInfo.copy(playerId = playerId))
    // We either disconnect because of the player info or get a world info response
    connection.send(WorldInfoRequestPacket)
    return true
  }

  override fun handle(packet: WorldInfoPacket): Boolean {
    debug { "World info" }
    approveConnection()
    return true
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    debug { "Player info" }
    if (playerId != packet.playerId)
      return false
    approveConnection()
    return true
  }

  private fun approveConnection() {
    debug { "Approve connection: $playerId" }
    val playerId = playerId ?: return
    // Connection was approved so the client version was accepted
    connection.protocol = protocol[MultistateProtocol.State.Play]
    future.complete(ServerInitConnectionResult.Success(playerId))
    // Sending this packet triggers the client to request all the information
    // from the server once again, this allows it to request and load a new world.
    clientConnection.send(ConnectionApprovedPacket(playerId))
    this.playerId = null
  }

  override fun handleGeneric(packet: Packet) {
    debug { "Received unexpected packet: $packet" }
  }

  override fun handleUnknown(packet: ByteBuf) {
    debug { "Received unexpected packet." }
  }

  private fun debug(message: () -> String) {
    Terre.logger.debug { "[${connection.localAddress}] ${message()}" }
  }
}
