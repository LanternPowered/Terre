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
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.ServerConnectionRequestResult
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.ConnectionHandler
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.KeepAlivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerBuffsPacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHealthPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.packet.PlayerManaPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.text
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

  private fun handleCommand(command: String): Boolean {
    val split = command.split(" ").filter { it.isNotEmpty() }
    if (split.isEmpty())
      return false

    val id = split[0]
    val args = split.subList(1, split.size)

    if (id == "connect") {
      handleConnectCommand(args)
      return true
    }
    return false
  }

  private fun handleConnectCommand(args: List<String>) {
    var name = args.getOrNull(0)
    fun send(text: Text) {
      this.playerImpl.sendMessage(Terre.message(text))
    }
    if (name == null) {
      send("Please specify a target server.".text())
      return
    }
    // TODO: Use command framework
    val server = Proxy.servers[name]
    if (server != null) {
      name = server.info.name
      send(textOf("Attempting to connect to $name."))
      this.playerImpl.connectToWithFuture(server)
          .whenComplete { result, _ ->
            val message = if (result != null) {
              when (result) {
                is ServerConnectionRequestResult.Success -> {
                  textOf("Successfully connected to $name.")
                }
                is ServerConnectionRequestResult.Disconnected -> {
                  textOf("Failed to connect to $name") +
                      (result.reason?.also { ": ".text() + it } ?: textOf())
                }
                is ServerConnectionRequestResult.AlreadyConnected -> {
                  textOf("You're already connected to $name.")
                }
                is ServerConnectionRequestResult.ConnectionInProgress -> {
                  textOf("You're already connecting to another server.")
                }
              }
            } else textOf("Failed to connect to $name.")
            send(message)
          }
    } else {
      send(textOf("The server $name doesn't exist."))
    }
  }

  override fun handle(packet: PlayerCommandPacket): Boolean {
    Terre.logger.info { "Received: $packet" }
    if (packet.commandId == "Say") {
      val command = packet.arguments
      if (command.startsWith("/")) {
        if (handleCommand(command.substring(1))) {
          return true
        }
      }
    }
    return false // Forward
  }

  override fun handle(packet: ClientUniqueIdPacket): Boolean {
    check(packet.uniqueId == this.playerImpl.uniqueId)
    return false // Forward
  }

  override fun handle(packet: PlayerSpawnPacket): Boolean {
    this.playerImpl.serverConnection?.isWorldInitialized = true
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
    val serverConnection = this.playerImpl.serverConnection ?: return
    // During this state, not all packets are allowed to pass through
    if (!serverConnection.isWorldInitialized && !isWorldInitPacket(packet))
      return
    // Terre.logger.info { "Forward to server: $packet" }
    serverConnection.ensureConnected().send(packet)
  }

  override fun handleUnknown(packet: ByteBuf) {
    val serverConnection = this.playerImpl.serverConnection ?: return
    // During this state, not all packets are allowed to pass through
    if (!serverConnection.isWorldInitialized)
      return
    serverConnection.ensureConnected().send(packet)
  }
}
