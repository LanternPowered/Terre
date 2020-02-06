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
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.ProtocolRegistry
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.textOf
import java.security.MessageDigest
import kotlin.streams.toList

/**
 * The connection handler that is used initially to establish a connection
 * between the client and the proxy server.
 */
internal class ClientInitConnectionHandler(
    val connection: Connection
) : ConnectionHandler {

  private enum class State {
    Init,
    Handshake,
    RequestClientInfo,
    RequestPassword,
    Done,
  }

  // Client Init

  // Init player id must be 16 in the case that the
  // mobile client check is implemented.

  // C -> S: ConnectionRequestPacket
  // E: ClientConnectEvent -> Disconnect if denied
  // S -> C: ConnectionApprovedPacket(playerId = 16)
  // C -> S: PlayerInfoPacket
  // C -> S: ClientUniqueIdPacket
  // C -> S: WorldInfoRequestPacket
  // E: ClientPreLoginEvent -> Disconnect if denied
  // If a password is requested
  //   S -> C: PasswordRequestPacket
  //   C -> S: PasswordResponsePacket
  // E: ClientLoginEvent -> Disconnect if denied
  // E: ClientPostLoginEvent

  // TODO: Is currently unneeded, since the mobile uses an older protocol version,
  //   but if the versions are equal and catch up, this could be a solution to check it.
  // For proper mobile integration checking, this method is based on the fact
  // that in the mobile versions are loops that range between 0..16 (exclusive)
  // while on desktop version 0..255 (exclusive) is used. The mobile client allows
  // player ids in the range 0..16 (inclusive) without crashing.
  // After C -> S: PasswordResponsePacket
  // S -> C: PlayerActivePacket(playerId = 16, active = true)
  // S -> C: PlayerActivePacket(playerId = 15, active = true)
  // S -> C: NebulaLevelUpRequestPacket(playerId = 15, levelUpType = 0, origin = [0;0])
  // S -> C: KeepAlivePacket
  // If C -> S: AddPlayerBuffPacket(playerId = 16)
  //   -> The client isn't mobile, because updates were send for player id 16
  // C -> S: KeepAlivePacket -> To continue if the player buff packet was never received
  // S -> C: PlayerActivePacket(playerId = 16, active = false)
  // S -> C: PlayerActivePacket(playerId = 15, active = false)

  private lateinit var protocolVersion: ProtocolVersion

  private lateinit var identifier: PlayerIdentifier
  private lateinit var name: String

  private lateinit var player: PlayerImpl
  private lateinit var expectedPassword: String

  private var state: State = State.Init

  private fun checkState(expected: State) {
    check(this.state == expected) {
      "Expected the state $expected, but the connection is currently on $state" }
  }

  override fun initialize() {
  }

  override fun disconnect() {
    // TODO
  }

  override fun handle(packet: ConnectionRequestPacket): Boolean {
    checkState(State.Init)
    this.state = State.Handshake
    val protocolVersion = packet.version
    if (protocolVersion !is ProtocolVersion.Vanilla) {
      // TODO: Modded support
      this.connection.close(textOf("Only vanilla clients are supported."))
      return true
    }
    val protocol = ProtocolRegistry[protocolVersion.protocol]
    if (protocol == null) {
      val expected = ProtocolRegistry.all.stream()
          .map { it.version }.toList()
          .joinToString(separator = ", ", prefix = "[", postfix = "]")
      this.connection.close(textOf(
          "The client isn't supported. Expected version of $expected, but the client is $protocolVersion."))
      return true
    }
    this.connection.initProtocol(protocol)
    this.protocolVersion = protocolVersion
    val inboundConnection = InitialInboundConnection(
        this.connection.remoteAddress, protocolVersion)
    TerreEventBus.postAsyncWithFuture(ClientConnectEvent(inboundConnection))
        .thenAcceptAsync({ event ->
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
    this.connection.send(ConnectionApprovedPacket(PlayerId(16)))
  }

  private fun continueLogin() {
    checkState(State.RequestClientInfo)
    this.player = PlayerImpl(this.connection, this.protocolVersion, this.name, this.identifier)

    TerreEventBus.postAsyncWithFuture(ClientPreLoginEvent(this.player))
        .thenAcceptAsync({ event ->
          val result = event.result
          if (result is ClientPreLoginEvent.Result.Denied) {
            this.state = State.Done
            this.connection.close(result.reason)
          } else if (result is ClientPreLoginEvent.Result.RequestPassword && result.password.isNotEmpty()) {
            this.state = State.RequestPassword
            this.expectedPassword = result.password
            this.connection.send(PasswordRequestPacket)
          } else {
            this.state = State.Done
            this.player.finishLogin(ClientLoginEvent.Result.Allowed)
          }
        }, this.connection.eventLoop)
  }

  override fun handle(packet: PasswordResponsePacket): Boolean {
    checkState(State.RequestPassword)
    val result = if (packet.password != this.expectedPassword) {
      ClientLoginEvent.Result.Denied(textOf("Invalid password."))
    } else {
      ClientLoginEvent.Result.Allowed
    }
    this.player.finishLogin(result)
    this.state = State.Done
    return true
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    checkState(State.RequestClientInfo)
    this.name = packet.playerName
    return true
  }

  override fun handle(packet: ClientUniqueIdPacket): Boolean {
    checkState(State.RequestClientInfo)
    // Generate a identifier that matches the
    // tShock player identifiers.
    val digest = MessageDigest.getInstance("SHA-512")
    digest.reset()
    digest.update(packet.uniqueId.toString().toByteArray(Charsets.UTF_8))
    this.identifier = PlayerIdentifier(digest.digest())
    return true
  }

  override fun handle(packet: WorldInfoRequestPacket): Boolean {
    // By now we should have received all information from the
    // client so we can initialize the play phase. And the client
    // can actually start connecting to backing servers.
    continueLogin()
    return true
  }

  override fun handleGeneric(packet: Packet) {
    // Discard everything
  }

  override fun handleUnknown(packet: ByteBuf) {
    // Discard everything
  }
}
