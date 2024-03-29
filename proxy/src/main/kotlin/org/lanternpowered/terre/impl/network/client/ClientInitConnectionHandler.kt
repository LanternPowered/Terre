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
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.event.connection.ClientConnectEvent
import org.lanternpowered.terre.event.connection.PlayerLoginEvent
import org.lanternpowered.terre.event.connection.PlayerPreLoginEvent
import org.lanternpowered.terre.event.permission.InitPermissionSubjectEvent
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.ProtocolRegistry
import org.lanternpowered.terre.impl.network.ProtocolVersions
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerPacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.network.packet.init.InitDisconnectClientPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.text.textOf
import java.util.UUID

/**
 * The connection handler that is used initially to establish a connection between the client and
 * the proxy server.
 */
internal class ClientInitConnectionHandler(
  private val connection: Connection
) : ConnectionHandler {

  private enum class State {
    Init,
    Handshake,
    RequestClientInfo,
    DetectClientPlayerLimit,
    RequestPassword,
    Done,
  }

  // Client Init

  // C -> S: ConnectionRequestPacket
  // E: ClientConnectEvent -> Disconnect if denied
  // S -> C: ConnectionApprovedPacket(playerId = 1)
  // C -> S: PlayerInfoPacket
  // C -> S: ClientUniqueIdPacket
  // C -> S: WorldInfoRequestPacket
  // S -> C: ItemRemoveOwnerPacket
  // C -> S: ItemUpdateOwnerPacket
  // E: ClientPreLoginEvent -> Disconnect if denied
  // If a password is requested
  //   S -> C: PasswordRequestPacket
  //   C -> S: PasswordResponsePacket
  // E: ClientLoginEvent -> Disconnect if denied
  // E: ClientPostLoginEvent

  private lateinit var protocolVersion: ProtocolVersion
  private lateinit var clientUniqueId: UUID
  private lateinit var name: String

  private lateinit var player: PlayerImpl
  private lateinit var expectedPassword: String

  private lateinit var playerInfo: PlayerInfoPacket

  private var state: State = State.Init

  private fun checkState(expected: State) {
    check(state == expected) {
      "Expected the state $expected, but the connection is currently on $state" }
  }

  override fun initialize() {
    Terre.logger.debug { "P <- C [${connection.remoteAddress}] Connected." }
  }

  override fun disconnect() {
  }

  override fun exception(throwable: Throwable) {
  }

  override fun handle(packet: ConnectionRequestPacket): Boolean {
    checkState(State.Init)
    Terre.logger.debug { "P <- C [${connection.remoteAddress}] Connection request: ${packet.version}" }
    state = State.Handshake
    val protocolVersion = ProtocolVersions.parse(packet.version)
    if (protocolVersion == null) {
      val reason = "Unknown protocol version: ${packet.version}".text()
      connection.close(reason) { InitDisconnectClientPacket(reason) }
      return true
    }
    val protocol = ProtocolRegistry[protocolVersion]
    if (protocol == null) {
      val expected = ProtocolRegistry.all.asSequence()
        .map { it.version }
        .joinToString(separator = ", ", prefix = "[", postfix = "]") {
          when (it) {
            is ProtocolVersion.Vanilla -> it.version.toString()
            is ProtocolVersion.TModLoader -> "tModLoader ${it.version}"
          }
        }
      // TODO: Modded support
      val reason = if (protocolVersion !is ProtocolVersion.Vanilla) {
        textOf("Only vanilla clients are supported.")
      } else {
        textOf("The client isn't supported.\nExpected version of $expected, " +
          "but the client is $protocolVersion.")
      }
      connection.close(reason) { InitDisconnectClientPacket(reason, protocolVersion) }
      return true
    }
    this.protocolVersion = protocolVersion
    connection.protocol = protocol
    val inboundConnection = InitialInboundConnection(connection.remoteAddress, protocolVersion)
    TerreEventBus.postAsyncWithFuture(ClientConnectEvent(inboundConnection))
      .thenAcceptAsync({ event ->
        if (connection.isClosed)
          return@thenAcceptAsync
        val result = event.result
        if (result is ClientConnectEvent.Result.Denied) {
          connection.close(result.reason)
        } else {
          startLogin()
        }
      }, connection.eventLoop)
    return true
  }

  private fun startLogin() {
    checkState(State.Handshake)
    state = State.RequestClientInfo
    // Send the approved packet, we just do this with a fixed id for now to receive information
    // from the client. When switching to the play mode the client will receive a new one.
    connection.send(ConnectionApprovedPacket(PlayerId(1)))
    Terre.logger.debug { "P -> C [${connection.remoteAddress}] Start login by collecting client info" }
  }

  private fun continueLogin(nonePlayerId: PlayerId) {
    connection.nonePlayerId = nonePlayerId
    player = PlayerImpl(connection, protocolVersion, name, clientUniqueId)
    player.lastPlayerInfo = playerInfo
    if (player.checkDuplicateIdentifier())
      return

    TerreEventBus.postAsyncWithFuture(InitPermissionSubjectEvent(player))
      .thenCompose { event ->
        player.permissionChecker = event.permissionChecker
        TerreEventBus.postAsyncWithFuture(PlayerPreLoginEvent(player))
      }
      .thenAcceptAsync({ event ->
        if (connection.isClosed)
          return@thenAcceptAsync
        val result = event.result
        if (result is PlayerPreLoginEvent.Result.Denied) {
          state = State.Done
          connection.close(result.reason)
        } else if (result is PlayerPreLoginEvent.Result.RequestPassword && result.password.isNotEmpty()) {
          state = State.RequestPassword
          expectedPassword = result.password
          connection.send(PasswordRequestPacket)
          Terre.logger.debug { "P -> C [${connection.remoteAddress},$name] Password request" }
        } else {
          state = State.Done
          player.finishLogin(PlayerLoginEvent.Result.Allowed)
        }
      }, connection.eventLoop)
  }

  override fun handle(packet: PasswordResponsePacket): Boolean {
    Terre.logger.debug { "P <- C [${connection.remoteAddress},$name] Password response" }
    checkState(State.RequestPassword)
    val result = if (packet.password != expectedPassword) {
      PlayerLoginEvent.Result.Denied(textOf("Invalid password."))
    } else {
      PlayerLoginEvent.Result.Allowed
    }
    state = State.Done
    player.finishLogin(result)
    return true
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    checkState(State.RequestClientInfo)
    name = packet.playerName
    playerInfo = packet
    Terre.logger.debug { "P <- C [${connection.remoteAddress}] Client player name: $name" }
    return true
  }

  override fun handle(packet: ClientUniqueIdPacket): Boolean {
    checkState(State.RequestClientInfo)
    clientUniqueId = packet.uniqueId
    return true
  }

  override fun handle(packet: WorldInfoRequestPacket): Boolean {
    checkState(State.RequestClientInfo)
    state = State.DetectClientPlayerLimit
    connection.send(ItemRemoveOwnerPacket(ItemRemoveOwnerPacket.PingPongItemId))
    Terre.logger.debug { "P <- C [${connection.remoteAddress}, $name] " +
      "Start detecting player limit" }
    return true
  }

  override fun handle(packet: ItemUpdateOwnerPacket): Boolean {
    if (packet.id != ItemRemoveOwnerPacket.PingPongItemId)
      return false
    checkState(State.DetectClientPlayerLimit)
    val nonePlayerId = packet.ownerId
    Terre.logger.debug { "P <- C [${connection.remoteAddress}, $name] " +
      "Detected client with player limit of ${nonePlayerId.value} players" }
    Terre.logger.debug { "P <- C [${connection.remoteAddress}, $name] " +
      "Client info sending done" }

    // By now we should have received all information from the client, so we can initialize the
    // play phase. And the client can actually start connecting to backing servers.
    continueLogin(nonePlayerId)
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
