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
import io.netty.util.AttributeKey
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.math.Vec2i
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.Protocol155
import org.lanternpowered.terre.impl.network.cache.DeathSourceInfoCache
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcNamePacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.impl.player.ServerConnectionImpl

internal open class ServerPlayConnectionHandler(
    private val serverConnection: ServerConnectionImpl,
    private val player: PlayerImpl
) : ConnectionHandler {

  companion object {

    private val notFirstServerConnection: AttributeKey<Boolean>
        = AttributeKey.valueOf("not-first-server-connection")
  }

  /**
   * The client connection.
   */
  private val clientConnection
    get() = this.player.clientConnection

  private var deathSourceInfoCache: DeathSourceInfoCache? = null

  override fun initialize() {
    initializeDeathSourceCache()
  }

  override fun disconnect() {
  }

  private fun initializeDeathSourceCache() {
    val connection = this.player.clientConnection
    // For the legacy protocol we need to keep track of some things to
    // translate death reasons from the new protocol to the old one.
    if (connection.protocol == Protocol155) {
      // Initialize every time a new connection is made, so
      // the cache gets cleared.
      this.deathSourceInfoCache = DeathSourceInfoCache(this.player.name)
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
      if (npc.type != packet.npcType || !npc.active) {
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

    val notFirstConnection = this.player.clientConnection.attr(notFirstServerConnection).getAndSet(true)
    if (notFirstConnection) {
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

  override fun handleGeneric(packet: Packet) {
    this.player.clientConnection.send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    this.player.clientConnection.send(packet)
  }
}
