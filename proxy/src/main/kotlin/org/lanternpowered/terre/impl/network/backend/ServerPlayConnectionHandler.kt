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
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.CustomPayloadPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.EssentialTilesRequestPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.PlayerTeamPacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdateNamePacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdatePacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.impl.player.ServerConnectionImpl
import org.lanternpowered.terre.impl.util.parseInetAddress
import org.lanternpowered.terre.impl.util.resolve
import org.lanternpowered.terre.math.Vec2i

internal open class ServerPlayConnectionHandler(
  private val serverConnection: ServerConnectionImpl,
  private val player: PlayerImpl,
) : ConnectionHandler {

  /**
   * The client connection.
   */
  private val clientConnection
    get() = player.clientConnection

  private val wasPreviouslyConnectedToServer = player.wasPreviouslyConnectedToServer

  private var sendRequestEssentialTiles = false

  override fun initialize() {
    player.wasPreviouslyConnectedToServer = true
  }

  override fun disconnect() {
    player.disconnectedFromServer(serverConnection)
  }

  override fun handle(packet: DisconnectPacket): Boolean {
    Terre.logger.info { "Got disconnect: $packet" }
    return true
  }

  override fun exception(throwable: Throwable) {
  }

  override fun handle(packet: WorldInfoPacket): Boolean {
    if (!sendRequestEssentialTiles) {
      sendRequestEssentialTiles = true
      // The client sends this the first time it connects to a server,
      // this time we need to fake it.
      if (wasPreviouslyConnectedToServer)
        serverConnection.ensureConnected().send(EssentialTilesRequestPacket(Vec2i(-1, -1)))
    }
    return false // Forward
  }

  override fun handle(packet: PlayerActivePacket): Boolean {
    player.trackedPlayers[packet.playerId].active = packet.active
    return false // Forward
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    player.trackedPlayers[packet.playerId].name = packet.playerName
    return false // Forward
  }

  override fun handle(packet: NpcUpdateNamePacket): Boolean {
    player.trackedNpcs[packet.npcId].name = packet.name
    return false // Forward
  }

  override fun handle(packet: NpcUpdatePacket): Boolean {
    val npc = player.trackedNpcs[packet.id]
    val npcType = packet.type
    if (npc.type != npcType || !npc.active) {
      npc.type = npcType
      npc.name = null
      npc.life = 1
    }
    if (packet.life != null)
      npc.life = packet.life
    return false // Forward
  }

  override fun handle(packet: PlayerTeamPacket): Boolean {
    player.team = packet.team
    return false // Forward
  }

  override fun handle(packet: ProjectileUpdatePacket): Boolean {
    val projectile = player.trackedProjectiles[packet.id]
    projectile.active = true
    projectile.owner = packet.owner
    return false // Forward
  }

  override fun handle(packet: ProjectileDestroyPacket): Boolean {
    player.trackedProjectiles[packet.id].active = false
    return false // Forward
  }

  override fun handle(packet: ItemUpdatePacket): Boolean {
    player.trackedItems[packet.id].type = packet.stack.type
    return false // Forward
  }

  override fun handle(packet: CompleteConnectionPacket): Boolean {
    val playerId = serverConnection.playerId ?: error("Player id isn't known.")

    if (wasPreviouslyConnectedToServer) {
      // Sending this packet makes sure that the player spawns, even if the client was previously
      // connected to another world. This will trigger the client to find a new spawn location.
      clientConnection.send(PlayerSpawnPacket(playerId,
        Vec2i.Zero, 0, 0, 0, PlayerSpawnPacket.Context.SpawningIntoWorld))
    } else {
      // Notify the client that the connection is complete, this will attempt to spawn the player
      // in the world, only affects the first time the client connects to a server.
      clientConnection.send(packet)
    }

    Terre.logger.debug { "P <- S(${serverConnection.server.info.name}) [${player.name}] Connection complete." }
    return true
  }

  /**
   * Implement "dimensions" compatible server switching which can be requested by backing servers.
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
      player.connectToWithFuture(server)

    if (success)
      return true

    buf.readerIndex(index)
    return false // Forward
  }

  override fun handleGeneric(packet: Packet) {
    clientConnection.send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    clientConnection.send(packet)
  }
}
