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
import io.netty.util.concurrent.ScheduledFuture
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.Team
import org.lanternpowered.terre.event.player.PlayerChangePvPEnabledEvent
import org.lanternpowered.terre.event.player.PlayerChangeTeamEvent
import org.lanternpowered.terre.event.player.PlayerRespawnEvent
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.command.CommandManagerImpl
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerPacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerBuffsPacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHealthPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.packet.PlayerManaPacket
import org.lanternpowered.terre.impl.network.packet.PlayerPvPPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.PlayerTeamPacket
import org.lanternpowered.terre.impl.network.packet.PlayerUpdatePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.KeepAliveDuringModReloadPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileResponsePacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDonePacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.UpdateModConfigRequestPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.UpdateModConfigResponsePacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.textOf
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class ClientPlayConnectionHandler(
  private val player: PlayerImpl
) : ConnectionHandler {

  companion object {

    /**
     * The timeout time before a session is closed due to not responding to a keep alive packet.
     */
    private val keepAliveTimeout = Duration.ofSeconds(15).toMillis()
  }

  private var keepAliveTime = -1L
  private var keepAliveTask: ScheduledFuture<*>? = null

  override fun initialize() {
    initializeKeepAliveTask()
    player.clientConnection.send(PlayerActivePacket(PlayerId.None, false))
  }

  override fun disconnect() {
    cleanupKeepAliveTask()
    player.cleanup()
    Terre.logger.debug { "[${player.clientConnection.remoteAddress}] Disconnected" }
  }

  override fun afterWrite(packet: Any) {
    if (player.statusCounter > 0) {
      player.statusCounter--
      val statusPacket = player.statusText
      if (statusPacket != null)
        player.clientConnection.send(statusPacket)
    }
    if (packet is StatusPacket)
      player.statusCounter += packet.statusMax
  }

  override fun exception(throwable: Throwable) {
  }

  private fun initializeKeepAliveTask() {
    val connection = player.clientConnection
    keepAliveTask = connection.eventLoop.scheduleAtFixedRate({
      if (keepAliveTime == -1L) {
        keepAliveTime = System.currentTimeMillis()
        connection.send(ItemRemoveOwnerPacket(ItemRemoveOwnerPacket.PingPongItemId))
      } else if (System.currentTimeMillis() - keepAliveTime > keepAliveTimeout) {
        connection.close(textOf("Timed out"))
      }
    }, 0, 750, TimeUnit.MILLISECONDS)
  }

  private fun cleanupKeepAliveTask() {
    keepAliveTask?.cancel(true)
    keepAliveTask = null
  }

  override fun handle(packet: ItemUpdateOwnerPacket): Boolean {
    if (packet.id == ItemRemoveOwnerPacket.PingPongItemId) {
      if (keepAliveTime != -1L) {
        player.latency = (System.currentTimeMillis() - keepAliveTime).toInt()
        keepAliveTime = -1L
      } else if (player.forwardNextOwnerUpdate) {
        player.forwardNextOwnerUpdate = false
        return false // Forward
      }
      return true
    }
    return false // Forward
  }

  override fun handle(packet: SyncModsDonePacket): Boolean {
    val serverConnection = player.serverConnection
    if (serverConnection != null) {
      val syncModNetIdsPacket = serverConnection.syncModNetIdsPacket
      if (syncModNetIdsPacket != null) {
        player.clientConnection.send(syncModNetIdsPacket)
        serverConnection.syncModNetIdsPacket = null
      }
      player.clientConnection.send(ConnectionApprovedPacket(serverConnection.playerId))
    }
    return true
  }

  override suspend fun handle(packet: PlayerCommandPacket): Boolean {
    if (packet.commandId == "Say") {
      val command = packet.arguments
      if (command.startsWith("/")) {
        if (CommandManagerImpl.execute(player, command.substring(1)))
          return true
      }
    }
    return false // Forward
  }

  override fun handle(packet: ClientUniqueIdPacket): Boolean {
    check(packet.uniqueId == player.clientUniqueId)
    val serverConnection = player.serverConnection
    serverConnection?.ensureConnected()?.send(ClientUniqueIdPacket(player.serverClientUniqueId))
    return true
  }

  override fun handle(packet: PlayerInfoPacket): Boolean {
    player.lastPlayerInfo = packet
    return false // Forward
  }

  override fun handle(packet: PlayerSpawnPacket): Boolean {
    val serverConnection = player.serverConnection?.ensureConnected()
    player.serverConnection?.isWorldInitialized = true
    player.position = packet.position.toFloat()

    if (packet.context == PlayerSpawnPacket.Context.SpawningIntoWorld) {
      serverConnection?.send(packet)
      val playerId = packet.playerId
      val team = player.team
      val pvp = player.pvpEnabled
      if (team != Team.None)
        serverConnection?.send(PlayerTeamPacket(playerId, team))
      if (pvp)
        serverConnection?.send(PlayerPvPPacket(playerId, true))
      return true
    }

    if (player.health == 0 && packet.context == PlayerSpawnPacket.Context.ReviveFromDeath) {
      TerreEventBus.postAsyncWithFuture(PlayerRespawnEvent(player))
        .whenCompleteAsync({ _, exception ->
          if (exception != null) {
            Terre.logger.error("Failed to handle player death event", exception)
          } else {
            serverConnection?.send(packet)
          }
        }, player.clientConnection.eventLoop)
      return true // Do not forward
    }

    return false // Forward
  }

  override fun handle(packet: PlayerUpdatePacket): Boolean {
    player.position = packet.position
    return false // Forward
  }

  override fun handle(packet: PlayerInventorySlotPacket): Boolean {
    player.setInventoryItem(packet.slot, packet.itemStack)
    return false // Forward
  }

  override fun handle(packet: PlayerTeamPacket): Boolean {
    if (packet.playerId != player.playerId)
      return false // Forward
    val currentTeam = player.team
    if (packet.team == currentTeam)
      return false // Forward
    TerreEventBus.postAsyncWithFuture(PlayerChangeTeamEvent(player, packet.team))
      .thenAcceptAsync({ event ->
        if (!event.cancelled) {
          player.teamValue = packet.team
          player.serverConnection?.ensureConnected()?.send(packet)
        } else {
          player.clientConnection.send(packet.copy(team = currentTeam))
        }
      }, player.clientConnection.eventLoop)
    return true
  }

  override fun handle(packet: PlayerHealthPacket): Boolean {
    if (packet.playerId == player.playerId) {
      val connection = player.serverConnection?.ensureConnected()
      if (connection != null) {
        return player.handleHealth(packet, connection)
      }
    }
    return false
  }

  override fun handle(packet: PlayerPvPPacket): Boolean {
    if (packet.playerId != player.playerId)
      return false // Forward
    val currentPvPEnabled = player.pvpEnabled
    if (packet.enabled == currentPvPEnabled)
      return false // Forward
    TerreEventBus.postAsyncWithFuture(PlayerChangePvPEnabledEvent(player, packet.enabled))
      .thenAcceptAsync({ event ->
        if (!event.cancelled) {
          player.pvpEnabledValue = packet.enabled
          player.serverConnection?.ensureConnected()?.send(packet)
        } else {
          player.clientConnection.send(packet.copy(enabled = currentPvPEnabled))
        }
      }, player.clientConnection.eventLoop)
    return true
  }

  private fun isWorldInitPacket(packet: Packet): Boolean {
    return packet is PlayerInfoPacket ||
      packet is PlayerSpawnPacket ||
      packet is PlayerManaPacket ||
      packet is PlayerHealthPacket ||
      packet is PlayerBuffsPacket ||
      packet is PlayerInventorySlotPacket ||
      packet is WorldInfoRequestPacket ||
      packet is ClientUniqueIdPacket ||
      (packet is ItemUpdateOwnerPacket && packet.id == ItemRemoveOwnerPacket.PingPongItemId) ||
      packet is SyncModsPacket ||
      packet is SyncModsDonePacket ||
      packet is ModFileRequestPacket ||
      packet is ModFileResponsePacket ||
      packet is ModDataPacket ||
      packet is UpdateModConfigRequestPacket ||
      packet is UpdateModConfigResponsePacket ||
      packet is KeepAliveDuringModReloadPacket
  }

  override fun handleGeneric(packet: Packet) {
    val serverConnection = player.serverConnection ?: return
    // During this state, not all packets are allowed to pass through
    if (!serverConnection.isWorldInitialized && !isWorldInitPacket(packet))
      return
    // Terre.logger.info { "Forward to server: $packet" }
    serverConnection.ensureConnected().send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    val serverConnection = player.serverConnection ?: return
    // During this state, not all packets are allowed to pass through
    if (!serverConnection.isWorldInitialized)
      return
    serverConnection.ensureConnected().send(packet)
  }
}
