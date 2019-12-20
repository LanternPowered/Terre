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
import org.lanternpowered.terre.impl.ProxyImpl
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
import org.lanternpowered.terre.text.textOf
import java.util.UUID
import kotlin.streams.toList

/**
 * The connection handler that is used initially to establish a connection
 * between the client and the proxy server.
 */
class ClientInitConnectionHandler(
    val inboundConnection: InitialInboundConnection,
    val connection: Connection
) : ConnectionHandler {

  private var uniqueId: UUID? = null
  private var name: String? = null

  override fun initialize() {
  }

  override fun disconnect() {
    // TODO
  }

  override fun handle(packet: ConnectionRequestPacket): Boolean {
    val currentVersion = packet.version
    val protocol = ProtocolRegistry[currentVersion]
    if (protocol == null) {
      val expected = ProtocolRegistry.all.stream()
          .map { it.version }.toList()
          .joinToString(separator = ", ", prefix = "[", postfix = "]")
      this.connection.close(textOf(
          "The client isn't supported. Expected version of $expected, but the client is $currentVersion."))
      return true
    }
    this.connection.initProtocol(protocol)
    val maxPlayers = ProxyImpl.maxPlayers
    val currentPlayers = ProxyImpl.players.size
    if (currentPlayers >= maxPlayers) {
      this.connection.close(textOf("The server is full."))
      return true
    }
    val password = ProxyImpl.password
    if (password.isNotBlank()) {
      this.connection.send(PasswordRequestPacket)
    } else {
      initPlayPhase()
    }
    return true
  }

  override fun handle(packet: PasswordResponsePacket): Boolean {
    val expected = ProxyImpl.password
    if (expected.isNotBlank() && packet.password != expected) {
      this.connection.close(textOf("Invalid password."))
    } else {
      approve()
    }
    return true
  }

  private fun approve() {
    // Send the approved packet, we just do this with a fixed
    // id for now to receive information from the client. When
    // switching to the play mode the client will receive a new
    // one.
    this.connection.send(ConnectionApprovedPacket(PlayerId(1)))
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    this.name = packet.playerName
    return true
  }

  override fun handle(packet: ClientUniqueIdPacket): Boolean {
    this.uniqueId = packet.uniqueId
    return true
  }

  override fun handle(packet: WorldInfoRequestPacket): Boolean {
    // By now we should have received all information from the
    // client so we can initialize the play phase. And the client
    // can actually start connecting to backing servers.
    initPlayPhase()
    return true
  }

  private fun initPlayPhase() {

  }

  override fun handleGeneric(packet: Packet) {
    // Discard everything
  }

  override fun handleUnknown(packet: ByteBuf) {
    // Discard everything
  }
}
