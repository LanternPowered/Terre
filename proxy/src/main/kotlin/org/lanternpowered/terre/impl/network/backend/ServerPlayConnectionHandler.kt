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
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.event.chat.ServerChatEvent
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.CustomPayloadPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.EssentialTilesRequestPacket
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdatePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.PlayerTeamPacket
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdatePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.TeleportPylonPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.impl.player.ServerConnectionImpl
import org.lanternpowered.terre.impl.util.parseInetAddress
import org.lanternpowered.terre.impl.util.resolve
import org.lanternpowered.terre.math.Vec2i
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import java.util.UUID

internal open class ServerPlayConnectionHandler(
  private val serverConnection: ServerConnectionImpl,
  private val player: PlayerImpl,
) : ConnectionHandler {

  private val clientConnection
    get() = player.clientConnection

  private val wasPreviouslyConnectedToServer = player.wasPreviouslyConnectedToServer

  private var sendRequestEssentialTiles = false
  private var worldUniqueId: UUID? = null

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
    if (worldUniqueId != packet.uniqueId) {
      // Only reinitialize if the world changed (servers)
      worldUniqueId = packet.uniqueId
      Terre.logger.debug {
        "P <- S(${serverConnection.server.info.name}) [${player.name}] " +
          "Initializing proxy side character..."
      }
      val serverSideCharacter = player.updateServerSideCharacter(packet.serverSideCharacter)
      clientConnection.send(packet.copy(
        name = ProxyImpl.name,
        serverSideCharacter = serverSideCharacter,
      ))
      // Load proxy side character, updates client and server, inventory is loaded async and then
      // packets are sent
      player.loadAndInitCharacter()
    } else {
      clientConnection.send(packet.copy(name = ProxyImpl.name))
    }
    return true // Do not forward
  }

  override fun handle(packet: PlayerActivePacket): Boolean {
    player.trackedPlayers[packet.playerId].active = packet.active
    return false // Forward
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    player.trackedPlayers[packet.playerId].name = packet.playerName
    return false // Forward
  }

  override fun handle(packet: NpcUpdatePacket): Boolean {
    val npc = player.trackedNpcs[packet.id]
    val npcType = packet.type
    if (npc.type != npcType || !npc.active) {
      npc.type = npcType
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
    player.trackedProjectiles[packet.id].reset()
    return false // Forward
  }

  override fun handle(packet: ItemUpdatePacket): Boolean {
    player.trackedItems[packet.id].itemStack = packet.itemStack
    return false // Forward
  }

  override fun handle(packet: ItemUpdateOwnerPacket): Boolean {
    player.trackedItems[packet.id].owner = packet.ownerId
    return false // Forward
  }

  override fun handle(packet: ItemRemoveOwnerPacket): Boolean {
    player.trackedItems[packet.id].owner = PlayerId.None
    // tShock sends this packet with item id 400 after server side character data is loaded
    // to know that client packets can be accepted again
    if (packet.id == ItemRemoveOwnerPacket.PingPongItemId) {
      // normally this packet interferes with the keep alive system, so mark the next response
      // to make sure it gets forwarded
      player.forwardNextOwnerUpdate = true
    }
    return false // Forward
  }

  override fun handle(packet: TeleportPylonPacket): Boolean {
    if (packet.action == TeleportPylonPacket.Action.Added) {
      player.trackedTeleportPylons.put(packet.type, packet.position)
    } else if (packet.action == TeleportPylonPacket.Action.Removed) {
      player.trackedTeleportPylons.remove(packet.type)
    }
    return false // Forward
  }

  override fun handle(packet: PlayerInventorySlotPacket): Boolean {
    player.setInventoryItem(packet.slot, packet.itemStack)
    return false // Forward
  }

  override fun handle(packet: CompleteConnectionPacket): Boolean {
    val playerId = serverConnection.playerId

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

  override fun handle(packet: StatusPacket): Boolean {
    return packet.statusMax != 0 || player.statusText != null
  }

  /**
   * Implement "dimensions" compatible server switching which can be requested by backing servers.
   */
  override fun handle(packet: CustomPayloadPacket): Boolean {
    val buf = packet.content
    val index = buf.readerIndex()
    if (buf.readableBytes() < Short.SIZE_BYTES)
      return false // Forward

    var server: Server? = null
    var valid = false
    try {
      val servers = ProxyImpl.servers
      when (buf.readUnsignedShortLE()) {
        2 -> {
          // By name
          val name = buf.readString()
          if (buf.readableBytes() == 0) {
            valid = true
            server = servers[name]
          }
        }
        3 -> {
          // By server info
          val ip = buf.readString()
          val port = buf.readUnsignedShortLE()
          if (buf.readableBytes() == 0) {
            valid = true
            val address = parseInetAddress("$ip:$port").resolve()
            server = servers.find { it.info.address == address } ?: run {
              val info = ServerInfo(address.hostName, address)
              servers.register(info)
            }
          }
        }
      }
    } catch (_: Exception) {
    }

    if (server != null)
      player.connectToWithFuture(server)

    if (valid)
      return true

    buf.readerIndex(index)
    return false // Forward
  }

  override fun handle(packet: PlayerChatMessagePacket): Boolean {
    val sender = if (packet.authorId != PlayerId.None) {
      serverConnection.server.players
        .find { player -> (player as PlayerImpl).playerId == packet.authorId }
        ?: MessageSender.Unknown
    } else null
    handleChat(packet, packet.text, sender)
    return true
  }

  override fun handle(packet: ChatMessagePacket): Boolean {
    handleChat(packet, packet.text, null)
    return true
  }

  private fun handleChat(packet: Packet, message: Text, sender: MessageSender?) {
    val server = serverConnection.server
    TerreEventBus.postAsyncWithFuture(ServerChatEvent(player, server, message, sender))
      .whenCompleteAsync({ event, exception ->
        if (exception != null) {
          Terre.logger.error("Failed to handle server chat event", exception)
        } else if (!event.cancelled) {
          clientConnection.send(packet)
        }
      }, clientConnection.eventLoop)
  }

  override fun handleGeneric(packet: Packet) {
    clientConnection.send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    clientConnection.send(packet)
  }
}
