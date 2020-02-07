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
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.packet.KeepAlivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.textOf
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class ClientPlayConnectionHandler(
    private val playerImpl: PlayerImpl
) : ConnectionHandler {

  companion object {

    /**
     * The timeout time before a session is closed
     * due to not responding to a keep alive packet.
     */
    private val keepAliveTimeout = Duration.ofSeconds(15).toMillis()
  }

  private var keepAliveTime = -1L
  private var keepAliveTask: ScheduledFuture<*>? = null

  override fun initialize() {
    initializeKeepAliveTask()
  }

  override fun disconnect() {
    cleanupKeepAliveTask()
    this.playerImpl.cleanup()
    Terre.logger.debug { "[${playerImpl.clientConnection.remoteAddress}] Disconnected" }
  }

  override fun exception(throwable: Throwable) {
  }

  private fun initializeKeepAliveTask() {
    val connection = this.playerImpl.clientConnection
    this.keepAliveTask = connection.eventLoop.scheduleAtFixedRate({
      if (this.keepAliveTime == -1L) {
        this.keepAliveTime = System.currentTimeMillis()
        connection.send(KeepAlivePacket)
      } else if (System.currentTimeMillis() - this.keepAliveTime > keepAliveTimeout) {
        connection.close(textOf("Timed out"))
      }
    }, 0, 750, TimeUnit.MILLISECONDS)
  }

  private fun cleanupKeepAliveTask() {
    this.keepAliveTask?.cancel(true)
    this.keepAliveTask = null
  }

  override fun handle(packet: KeepAlivePacket): Boolean {
    this.playerImpl.latency = (System.currentTimeMillis() - this.keepAliveTime).toInt()
    this.keepAliveTime = -1L
    return true
  }

  override fun handle(packet: PlayerCommandPacket): Boolean {
    // TODO: Handle commands
    return false // Forward
  }

  override fun handleGeneric(packet: Packet) {
    val connection = this.playerImpl.serverConnection?.connection ?: return
    connection.send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    val connection = this.playerImpl.serverConnection?.connection ?: return
    connection.send(packet)
  }
}
