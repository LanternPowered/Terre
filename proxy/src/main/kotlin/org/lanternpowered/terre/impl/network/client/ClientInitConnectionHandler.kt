/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.client

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.event.connection.ClientConnectEvent
import org.lanternpowered.terre.event.connection.ClientLoginEvent
import org.lanternpowered.terre.event.connection.ClientPreLoginEvent
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.MultistateProtocol
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.ProtocolRegistry
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.IsMobileRequestPacket
import org.lanternpowered.terre.impl.network.packet.IsMobileResponsePacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.textOf
import java.security.MessageDigest
import java.util.*
import kotlin.streams.toList

/**
 * The connection handler that is used initially to establish a connection
 * between the client and the proxy server.
 */
internal class ClientInitConnectionHandler(
    private val connection: Connection
) : ConnectionHandler {

  private enum class State {
    Init,
    Handshake,
    RequestClientInfo,
    DetectMobile,
    RequestPassword,
    Done,
  }

  // Client Init

  // C -> S: ConnectionRequestPacket
  // E: ClientConnectEvent -> Disconnect if denied
  // S -> C: ConnectionApprovedPacket(playerId = 16)
  // C -> S: PlayerInfoPacket
  // C -> S: ClientUniqueIdPacket
  // C -> S: WorldInfoRequestPacket
  // S -> C: IsMobileRequestPacket
  // C -> S: IsMobileResponsePacket
  // E: ClientPreLoginEvent -> Disconnect if denied
  // If a password is requested
  //   S -> C: PasswordRequestPacket
  //   C -> S: PasswordResponsePacket
  // E: ClientLoginEvent -> Disconnect if denied
  // E: ClientPostLoginEvent

  private lateinit var protocolVersion: ProtocolVersion
  private lateinit var protocol: MultistateProtocol

  private lateinit var uniqueId: UUID
  private lateinit var name: String

  private lateinit var player: PlayerImpl
  private lateinit var expectedPassword: String

  private var state: State = State.Init

  private fun checkState(expected: State) {
    check(this.state == expected) {
      "Expected the state $expected, but the connection is currently on $state" }
  }

  override fun initialize() {
    Terre.logger.info("P <- C [${connection.remoteAddress}] Connected.")
  }

  override fun disconnect() {
  }

  override fun exception(throwable: Throwable) {
  }

  override fun handle(packet: ConnectionRequestPacket): Boolean {
    checkState(State.Init)
    Terre.logger.debug { "P <- C [${connection.remoteAddress}] Connection request: ${packet.version}" }
    this.state = State.Handshake
    val protocolVersion = packet.version
    if (protocolVersion !is ProtocolVersion.Vanilla) {
      // TODO: Modded support
      this.connection.close(textOf("Only vanilla clients are supported."))
      return true
    }
    val protocol = ProtocolRegistry[protocolVersion]
    if (protocol == null) {
      val expected = ProtocolRegistry.all.stream()
          .map { it.version }.toList()
          .joinToString(separator = ", ", prefix = "[", postfix = "]") {
            when (it) {
              is ProtocolVersion.Vanilla -> it.version.toString()
              is ProtocolVersion.TModLoader -> "tModLoader ${it.version}"
            }
          }
      this.connection.close(textOf(
          "The client isn't supported. Expected version of $expected, but the client is $protocolVersion."))
      return true
    }
    this.protocol = protocol
    this.connection.protocol = protocol[MultistateProtocol.State.ClientInit]
    this.protocolVersion = protocolVersion
    val inboundConnection = InitialInboundConnection(
        this.connection.remoteAddress, protocolVersion)
    TerreEventBus.postAsyncWithFuture(ClientConnectEvent(inboundConnection))
        .thenAcceptAsync({ event ->
          if (this.connection.isClosed)
            return@thenAcceptAsync
          val result = event.result
          if (result is ClientConnectEvent.Result.Denied) {
            this.connection.close(result.reason)
          } else {
            startLogin()
          }
        }, this.connection.eventLoop)
    return true
  }

  private fun startLogin() {
    checkState(State.Handshake)
    this.state = State.RequestClientInfo
    // Send the approved packet, we just do this with a fixed
    // id for now to receive information from the client. When
    // switching to the play mode the client will receive a new
    // one.
    this.connection.send(ConnectionApprovedPacket(PlayerId(1)))
    Terre.logger.debug { "P -> C [${connection.remoteAddress}] Start login by collecting client info" }
  }

  private fun continueLogin(isMobile: Boolean) {
    // Generate a identifier that matches the tShock player identifiers.
    val digest = MessageDigest.getInstance("SHA-512")
    digest.reset()
    digest.update(this.uniqueId.toString().toByteArray(Charsets.UTF_8))
    val identifier = PlayerIdentifier(digest.digest())

    this.connection.isMobile = isMobile
    this.player = PlayerImpl(this.connection, this.protocolVersion,
        this.protocol, this.name, identifier, isMobile, this.uniqueId)
    if (this.player.checkDuplicateIdentifier())
      return

    TerreEventBus.postAsyncWithFuture(ClientPreLoginEvent(this.player))
        .thenAcceptAsync({ event ->
          if (this.connection.isClosed)
            return@thenAcceptAsync
          val result = event.result
          if (result is ClientPreLoginEvent.Result.Denied) {
            this.state = State.Done
            this.connection.close(result.reason)
          } else if (result is ClientPreLoginEvent.Result.RequestPassword && result.password.isNotEmpty()) {
            this.state = State.RequestPassword
            this.expectedPassword = result.password
            this.connection.send(PasswordRequestPacket)
            Terre.logger.debug { "P -> C [${connection.remoteAddress},$name] Password request" }
          } else {
            this.state = State.Done
            this.player.finishLogin(ClientLoginEvent.Result.Allowed)
          }
        }, this.connection.eventLoop)
  }

  override fun handle(packet: PasswordResponsePacket): Boolean {
    Terre.logger.debug { "P <- C [${connection.remoteAddress},$name] Password response" }
    checkState(State.RequestPassword)
    val result = if (packet.password != this.expectedPassword) {
      ClientLoginEvent.Result.Denied(textOf("Invalid password."))
    } else {
      ClientLoginEvent.Result.Allowed
    }
    this.state = State.Done
    this.player.finishLogin(result)
    return true
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    checkState(State.RequestClientInfo)
    this.name = packet.playerName
    Terre.logger.debug { "P <- C [${connection.remoteAddress}] Client player name: $name" }
    return true
  }

  override fun handle(packet: ClientUniqueIdPacket): Boolean {
    checkState(State.RequestClientInfo)
    this.uniqueId = packet.uniqueId
    return true
  }

  override fun handle(packet: WorldInfoRequestPacket): Boolean {
    checkState(State.RequestClientInfo)
    this.state = State.DetectMobile
    this.connection.send(IsMobileRequestPacket)
    Terre.logger.debug { "P <- C [${connection.remoteAddress}, $name] Start detecting mobile" }
    return true
  }

  override fun handle(packet: IsMobileResponsePacket): Boolean {
    checkState(State.DetectMobile)
    val isMobile = packet.isMobile

    Terre.logger.debug {
      val type = if (isMobile) "mobile" else "desktop"
      "P <- C [${connection.remoteAddress}, $name] Detected $type client"
    }
    Terre.logger.debug { "P <- C [${connection.remoteAddress}, $name] Client info sending done" }

    // By now we should have received all information from the
    // client so we can initialize the play phase. And the client
    // can actually start connecting to backing servers.
    continueLogin(isMobile)
    return true
  }

  override fun handleGeneric(packet: Packet) {
    // Discard everything
    // Terre.logger.debug { "Received unexpected packet: $packet" }
  }

  override fun handleUnknown(packet: ByteBuf) {
    // Discard everything
    // Terre.logger.debug { "Received unexpected packet." }
  }
}
