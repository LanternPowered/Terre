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
import org.lanternpowered.terre.impl.network.packet.KeepAlivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerBuffsPacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHealthPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.packet.PlayerManaPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.PlayerUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.textOf
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class ClientPlayConnectionHandler(
  private val playerImpl: PlayerImpl
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
    playerImpl.clientConnection.send(PlayerActivePacket(PlayerId.None, false))
  }

  override fun disconnect() {
    cleanupKeepAliveTask()
    playerImpl.cleanup()
    Terre.logger.debug { "[${playerImpl.clientConnection.remoteAddress}] Disconnected" }
  }

  override fun exception(throwable: Throwable) {
  }

  private fun initializeKeepAliveTask() {
    val connection = playerImpl.clientConnection
    keepAliveTask = connection.eventLoop.scheduleAtFixedRate({
      if (keepAliveTime == -1L) {
        keepAliveTime = System.currentTimeMillis()
        connection.send(KeepAlivePacket)
      } else if (System.currentTimeMillis() - keepAliveTime > keepAliveTimeout) {
        connection.close(textOf("Timed out"))
      }
    }, 0, 750, TimeUnit.MILLISECONDS)
  }

  private fun cleanupKeepAliveTask() {
    keepAliveTask?.cancel(true)
    keepAliveTask = null
  }

  override fun handle(packet: KeepAlivePacket): Boolean {
    playerImpl.latency = (System.currentTimeMillis() - keepAliveTime).toInt()
    keepAliveTime = -1L
    return true
  }

  override suspend fun handle(packet: PlayerCommandPacket): Boolean {
    if (packet.commandId == "Say") {
      val command = packet.arguments
      if (command.startsWith("/")) {
        if (CommandManagerImpl.execute(playerImpl, command.substring(1)))
          return true
      }
    }
    return false // Forward
  }

  override fun handle(packet: ClientUniqueIdPacket): Boolean {
    check(packet.uniqueId == playerImpl.uniqueId)
    return false // Forward
  }

  override fun handle(packet: PlayerSpawnPacket): Boolean {
    playerImpl.serverConnection?.isWorldInitialized = true
    playerImpl.position = packet.position.toFloat()
    return false // Forward
  }

  override fun handle(packet: PlayerUpdatePacket): Boolean {
    playerImpl.position = packet.position
    return false // Forward
  }

  override fun handle(packet: ProjectileDestroyPacket): Boolean {
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
      packet is ClientUniqueIdPacket
  }

  override fun handleGeneric(packet: Packet) {
    val serverConnection = playerImpl.serverConnection ?: return
    // During this state, not all packets are allowed to pass through
    if (!serverConnection.isWorldInitialized && !isWorldInitPacket(packet))
      return
    // Terre.logger.info { "Forward to server: $packet" }
    serverConnection.ensureConnected().send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    val serverConnection = playerImpl.serverConnection ?: return
    // During this state, not all packets are allowed to pass through
    if (!serverConnection.isWorldInitialized)
      return
    serverConnection.ensureConnected().send(packet)
  }
}
