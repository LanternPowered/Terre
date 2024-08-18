/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import io.netty.buffer.Unpooled
import org.lanternpowered.terre.MaxPlayers
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.buffer.writeString
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.NetworkInterface
import kotlin.math.min
import kotlin.streams.asSequence

/**
 * Broadcasts the server to the LAN network.
 */
internal class ProxyBroadcastTask(private val proxy: Proxy) {

  private lateinit var socket: DatagramSocket

  fun init() {
    try {
      socket = DatagramSocket()
      socket.broadcast = true
    } catch (ex: IOException) {
      Terre.logger.error("Failed to open socket, the LAN broadcast will NOT work.", ex)
      return
    }

    val thread = Thread(::broadcast, "lan-broadcast")
    thread.isDaemon = true
    thread.start()

    Terre.logger.info("Started to broadcast the server on the LAN.")
  }

  fun stop() {
    socket.close()
  }

  private fun broadcast() {
    var errorLogged = false
    while (!socket.isClosed) {
      try {
        val broadcastDataList = listOf(
          broadcastData(true),
          broadcastData(false),
        )
        NetworkInterface.networkInterfaces()
          .asSequence()
          .filter { itf -> !itf.isLoopback && itf.isUp }
          .flatMap { itf -> itf.interfaceAddresses.asSequence() }
          .map { address -> address.broadcast }
          .filterNotNull()
          .forEach { address ->
            for (broadcastData in broadcastDataList) {
              val array = broadcastData.array()
              socket.send(DatagramPacket(array, array.size, address, 8888))
            }
          }
        errorLogged = false
      } catch (ex: IOException) {
        if (!errorLogged) {
          Terre.logger.error("Failed to broadcast server on the LAN.", ex)
          errorLogged = true
        }
      }

      // Sleep 1 second, same as for vanilla servers
      Thread.sleep(1000L)
    }
  }

  private fun broadcastData(sendHardMode: Boolean) = Unpooled.buffer().apply {
    writeIntLE(1010)
    writeIntLE(proxy.address.port)
    writeString(proxy.name)
    writeString(proxy.address.hostString)
    writeShortLE(8400) // World size - from a large world
    writeBoolean(false) // Is crimson
    writeIntLE(0) // Game mode
    val maxPlayers = proxy.maxPlayers.let { if (it is MaxPlayers.Limited) it.amount else 255 }
    writeByte(min(maxPlayers, 255))
    writeByte(min(proxy.players.size, 255))
    if (sendHardMode) {
      writeBoolean(false) // Is hard mode
    }
  }
}
