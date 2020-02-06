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
import org.lanternpowered.terre.ServerConnectionRequestResult
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.Protocol
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.player.ServerConnectionImpl
import org.lanternpowered.terre.text.Text
import java.util.concurrent.CompletableFuture

/**
 * Represents the result of a [ServerInitConnectionHandler].
 */
internal data class ServerInitConnectionResult(
    val result: ServerConnectionRequestResult,
    val playerId: PlayerId? = null
)

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
    private val future: CompletableFuture<ServerInitConnectionResult>,
    versionsToAttempt: List<Pair<Protocol, ProtocolVersion>>
) : ConnectionHandler {

  private fun ServerConnectionRequestResult.asResult(playerId: PlayerId? = null)
      = ServerInitConnectionResult(this, playerId)

  private val player = this.connection.player
  private var playerId: PlayerId? = null

  private lateinit var attemptedVersion: ProtocolVersion
  private lateinit var attemptedProtocol: Protocol
  private val versionsToAttemptQueue = versionsToAttempt.toMutableList()
  private var firstDisconnectReason: Text? = null

  init {
    check(versionsToAttempt.isNotEmpty())
  }

  override fun initialize() {
    // Prioritize the last known client version if present, it's more likely that
    // the client will be allowed using this version.
    val lastKnownVersion = this.connection.server.lastKnownVersion
    if (lastKnownVersion != null && this.versionsToAttemptQueue.size > 1) {
      val pair = this.versionsToAttemptQueue
          .find { it.second == lastKnownVersion }
      if (pair != null) {
        this.versionsToAttemptQueue -= pair
        this.versionsToAttemptQueue.add(0, pair)
      }
    }
    // Try initial handshake
    tryNextHandshake()
  }

  private fun tryNextHandshake() {
    if (this.versionsToAttemptQueue.isEmpty()) {
      println("Trying protocol: Done")
      // No more versions to try, we are done
      this.future.complete(ServerConnectionRequestResult.Disconnected(
          this.connection.server, this.firstDisconnectReason).asResult())
    } else {
      // Try the next version
      val (protocol, version) = this.versionsToAttemptQueue.removeAt(0)
      val connection = this.connection.ensureConnected()
      connection.send(ConnectionRequestPacket(version))
      Terre.logger.debug { "P -> S(${this.connection.server.info.name}) [${player.name}] Connection request: $version" }
      this.attemptedVersion = version
      this.attemptedProtocol = protocol
    }
  }

  override fun disconnect() {
    println("Disconnect")
    if (!this.future.isDone)
      this.future.complete(ServerConnectionRequestResult.Disconnected(
          this.connection.server, null).asResult())
  }

  override fun handle(packet: DisconnectPacket): Boolean {
    Terre.logger.debug { "P <- S(${this.connection.server.info.name}) [${player.name}] Disconnect: ${packet.reason}" }
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
    val password = this.connection.server.info.password
    this.connection.ensureConnected().send(PasswordResponsePacket(password))
    Terre.logger.debug { "P <- S(${connection.server.info.name}) [${player.name}] Password request." }
    Terre.logger.debug { "P -> S(${connection.server.info.name}) [${player.name}] Password response: $password" }
    return true
  }

  override fun handle(packet: ConnectionApprovedPacket): Boolean {
    // Connection was approved so the client version was accepted
    this.connection.server.lastKnownVersion = this.attemptedVersion
    this.connection.ensureConnected().protocol = this.attemptedProtocol
    this.playerId = packet.playerId
    // Sending this packet triggers the client to request all the information
    // from the server once again, this allows it to request and load a new world.
    this.player.clientConnection.send(packet)
    // Connection is approved.
    this.future.complete(ServerConnectionRequestResult.Success(this.connection.server).asResult(playerId))
    Terre.logger.debug { "P <- S(${connection.server.info.name}) [${player.name}] Connection approved: $playerId" }
    return true
  }

  override fun handleGeneric(packet: Packet) {
    Terre.logger.warn("Received unexpected packet from the backend server: $packet")
  }

  override fun handleUnknown(packet: ByteBuf) {
    Terre.logger.warn("Received unexpected packet from the backend server.")
  }
}
