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

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.lanternpowered.terre.MaxPlayers
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.buffer.writeString
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.math.min

/**
 * Broadcasts the server to the LAN network.
 */
internal class ProxyBroadcastTask(private val proxy: Proxy) {

  private lateinit var socket: DatagramSocket

  fun init() {
    try {
      socket = DatagramSocket()
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
    val targetPort = 8888
    val targetAddress = InetAddress.getByName("255.255.255.255")

    var errorLogged = false

    while (!socket.isClosed) {
      val buf = Unpooled.buffer()
      buf.writeBroadcastData()
      val array = buf.array()
      try {
        socket.send(DatagramPacket(array, array.size, targetAddress, targetPort))
        errorLogged = false
      } catch (ex: IOException) {
        if (!errorLogged) {
          Terre.logger.error("Failed to broadcast server on the LAN.", ex)
          errorLogged = true
        }
      }

      // Sleep 1 second
      Thread.sleep(1000L)
    }
  }

  private fun ByteBuf.writeBroadcastData() {
    writeIntLE(1010)
    writeIntLE(proxy.address.port)
    writeString(proxy.name)
    writeString(proxy.address.hostString)
    writeShortLE(8400) // World size - from a large world
    writeBoolean(false) // Is crimson
    writeBoolean(false) // Is expert
    val maxPlayers = proxy.maxPlayers.let { if (it is MaxPlayers.Limited) it.amount else 255 }
    writeByte(min(maxPlayers, 255))
    writeByte(min(proxy.players.size, 255))
    writeBoolean(false) // Is hard mode
  }
}
