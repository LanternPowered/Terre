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
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.command.CommandManagerImpl
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerPacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerBuffsPacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHealthPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.packet.PlayerManaPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.PlayerUpdatePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
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
    player.serverConnection?.isWorldInitialized = true
    player.position = packet.position.toFloat()
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

  private fun isWorldInitPacket(packet: Packet): Boolean {
    return packet is PlayerInfoPacket ||
      packet is PlayerSpawnPacket ||
      packet is PlayerManaPacket ||
      packet is PlayerHealthPacket ||
      packet is PlayerBuffsPacket ||
      packet is PlayerInventorySlotPacket ||
      packet is WorldInfoRequestPacket ||
      packet is ClientUniqueIdPacket ||
      (packet is ItemUpdateOwnerPacket && packet.id == ItemRemoveOwnerPacket.PingPongItemId)
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
