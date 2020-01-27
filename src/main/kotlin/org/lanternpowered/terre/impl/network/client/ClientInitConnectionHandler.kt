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
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.network.ClientVersion
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

  private val inboundConnection = InitialInboundConnection(this.connection.remoteAddress)

  private var identifier: PlayerIdentifier? = null
  private var name: String? = null

  override fun initialize() {
  }

  override fun disconnect() {
    // TODO
  }

  override fun handle(packet: ConnectionRequestPacket): Boolean {
    val currentVersion = packet.version
    if (currentVersion !is ClientVersion.Vanilla) {
      // TODO: Modded support
      this.connection.close(textOf("Only vanilla clients are supported."))
      return true
    }
    val protocol = ProtocolRegistry[currentVersion.protocol]
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
      approve()
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
    val digest = MessageDigest.getInstance("SHA-512")
    digest.reset()
    digest.update(packet.bytes)
    this.identifier = PlayerIdentifier(digest.digest())
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
    val name = this.name ?: error("Player name is unknown.")
    val identifier = this.identifier ?: error("Player identifier is unknown.")
    val player = PlayerImpl(this.connection, name, identifier)
    this.connection.setConnectionHandler(ClientPlayConnectionHandler(player))
  }

  override fun handleGeneric(packet: Packet) {
    // Discard everything
  }

  override fun handleUnknown(packet: ByteBuf) {
    // Discard everything
  }
}
