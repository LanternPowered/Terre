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
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.ProtocolVersions
import org.lanternpowered.terre.impl.network.VersionedProtocol
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.RealIPPacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.text.LocalizedText
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * The initial connection handler that is used to establish a connection to a server.
 *
 * @property connection The server connection
 * @property future The future that will be notified of the connection result
 * @property versionedProtocol The versioned protocol to use to handshake
 */
internal class ServerInitConnectionHandler(
  private val connection: Connection,
  private val future: CompletableFuture<ServerInitConnectionResult>,
  private val versionedProtocol: VersionedProtocol,
  private val password: String,
  private val playerInfo: PlayerInfoPacket,
  private val modded: Boolean,
  private val clientIP: String,
) : ConnectionHandler {

  private var playerId: PlayerId? = null
  private var accepted = false

  override fun initialize() {
    val version = versionedProtocol.version
    connection.protocol = ServerInitProtocol
    connection.send(ConnectionRequestPacket(ProtocolVersions.toString(version)))
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
    val reason = packet.reason
    val result = if (accepted) {
      if (modded && reason is LocalizedText && reason.key == "LegacyMultiplayer.2") {
        // The server was not able to handle the RealIPPacket, so probably a vanilla server
        ServerInitConnectionResult.NotModded(reason)
      } else {
        ServerInitConnectionResult.Disconnected(reason)
      }
    } else {
      ServerInitConnectionResult.UnsupportedProtocol(reason)
    }
    future.complete(result)
    // Make sure that the connection gets closed
    connection.close()
    debug { "Disconnect: $reason" }
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
    this.accepted = true
    this.playerId = playerId
    // Send an empty client unique id for tShock, so it does not send character data
    // until we are done
    connection.send(ClientUniqueIdPacket(UUID(0L, 0L)))
    if (modded) {
      // Forward the original client ip address
      connection.send(RealIPPacket(clientIP))
    }
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

  override fun handle(packet: StatusPacket): Boolean {
    debug { "Status: ${packet.text}" }
    return true
  }

  private fun approveConnection() {
    debug { "Approve connection: $playerId" }
    val playerId = playerId ?: return
    // Connection was approved so the client version was accepted
    connection.protocol = versionedProtocol.protocol
    future.complete(ServerInitConnectionResult.Success(playerId))
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
