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
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.math.Vec2i
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.MultistateProtocol
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.Protocol155
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.cache.DeathSourceInfoCache
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.CustomPayloadPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.EssentialTilesRequestPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcNamePacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.impl.player.ServerConnectionImpl
import org.lanternpowered.terre.impl.util.parseInetAddress
import org.lanternpowered.terre.impl.util.resolve

internal open class ServerPlayConnectionHandler(
    private val serverConnection: ServerConnectionImpl,
    private val player: PlayerImpl
) : ConnectionHandler {

  /**
   * The client connection.
   */
  private val clientConnection
    get() = this.player.clientConnection

  private val wasPreviouslyConnectedToServer = this.player.wasPreviouslyConnectedToServer

  private var deathSourceInfoCache: DeathSourceInfoCache? = null
  private var sendRequestEssentialTiles = false

  override fun initialize() {
    initializeDeathSourceCache()
    this.player.wasPreviouslyConnectedToServer = true
  }

  override fun disconnect() {
    this.player.disconnectedFromServer(this.serverConnection)
  }

  override fun handle(packet: DisconnectPacket): Boolean {
    Terre.logger.info { "Got disconnect: $packet" }
    return true
  }

  override fun exception(throwable: Throwable) {
  }

  private fun initializeDeathSourceCache() {
    val legacyServerProtocol = Protocol155[MultistateProtocol.State.Play]
    val connection = this.serverConnection.ensureConnected()
    // For the legacy protocol we need to keep track of some things to
    // translate death reasons from the new protocol to the old one.
    if (this.clientConnection.protocol != legacyServerProtocol
        && connection.protocol == legacyServerProtocol) {
      // Initialize every time a new connection is made, so
      // the cache gets cleared.
      this.deathSourceInfoCache = DeathSourceInfoCache(this.player.name)
      this.clientConnection.attr(DeathSourceInfoCache.Attribute).set(this.deathSourceInfoCache)
      connection.attr(DeathSourceInfoCache.Attribute).set(this.deathSourceInfoCache)
    }
  }

  private inline fun updateDeathSourceCache(crossinline fn: DeathSourceInfoCache.() -> Unit) {
    val cache = this.deathSourceInfoCache
    if (cache != null) {
      this.player.clientConnection.eventLoop.execute {
        cache.fn()
      }
    }
  }

  override fun handle(packet: WorldInfoPacket): Boolean {
    updateDeathSourceCache {
      worldName = packet.name
    }
    if (!this.sendRequestEssentialTiles) {
      this.sendRequestEssentialTiles = true
      // The client sends this the first time it connects to a server,
      // this time we need to fake it.
      if (this.wasPreviouslyConnectedToServer) {
        this.serverConnection.ensureConnected().send(EssentialTilesRequestPacket(Vec2i(-1, -1)))
      }
    }
    return false // Forward
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    updateDeathSourceCache {
      players.names[packet.playerId] = packet.playerName
    }
    return false // Forward
  }

  override fun handle(packet: UpdateNpcNamePacket): Boolean {
    updateDeathSourceCache {
      npcs[packet.npcId].name = packet.name
    }
    return false // Forward
  }

  override fun handle(packet: UpdateNpcPacket): Boolean {
    updateDeathSourceCache {
      val npc = npcs[packet.npcId]
      val npcType = packet.npcType
      if (npc.type != npcType || !npc.active) {
        npc.type = npcType
        npc.name = null
        npc.active = true
      }
      if (packet.life ?: 0 < 0) {
        npc.active = false
      }
    }
    return false // Forward
  }

  override fun handle(packet: CompleteConnectionPacket): Boolean {
    val playerId = this.serverConnection.playerId ?: error("Player id isn't known.")

    if (this.wasPreviouslyConnectedToServer) {
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

    Terre.logger.debug { "P <- S(${serverConnection.server.info.name}) [${player.name}] Connection complete." }
    return true
  }

  /**
   * Implement "dimensions" compatible server switching which can be
   * requested by backing servers.
   */
  override fun handle(packet: CustomPayloadPacket): Boolean {
    val buf = packet.content
    val index = buf.readerIndex()
    if (buf.readableBytes() < Short.SIZE_BYTES)
      return false // Forward

    val (server, success) = try {
      val servers = ProxyImpl.servers
      when (buf.readUnsignedShortLE()) {
        2 -> {
          // By name
          val name = buf.readString()
          servers[name] to true
        }
        3 -> {
          // By server info
          val ip = buf.readString()
          val port = buf.readUnsignedShortLE()
          val address = parseInetAddress("$ip:$port").resolve()
          val server = servers.find { it.info.address == address } ?: run {
            val info = ServerInfo(address.hostName, address)
            servers.register(info)
          }
          server to true
        }
        else -> null to false
      }
    } catch (ex: Exception) {
      null to false
    }
    if (server != null)
      this.player.connectToWithFuture(server)

    if (success)
      return true

    buf.readerIndex(index)
    return false // Forward
  }

  override fun handleGeneric(packet: Packet) {
    this.player.clientConnection.send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    this.player.clientConnection.send(packet)
  }
}
