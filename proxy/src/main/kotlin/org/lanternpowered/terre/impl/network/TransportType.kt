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

import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.EpollDatagramChannel
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.kqueue.KQueueDatagramChannel
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.kqueue.KQueueSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.ThreadFactory

internal sealed class TransportType(
  val serverSocketChannelSupplier: () -> ServerSocketChannel,
  val socketChannelSupplier: () -> SocketChannel,
  val datagramChannelSupplier: () -> DatagramChannel,
  val eventLoopGroupSupplier: (threads: Int, threadFactory: ThreadFactory) -> EventLoopGroup
) {

  object Nio : TransportType(
    ::NioServerSocketChannel,
    ::NioSocketChannel,
    ::NioDatagramChannel,
    ::NioEventLoopGroup
  )

  object KQueue : TransportType(
    ::KQueueServerSocketChannel,
    ::KQueueSocketChannel,
    ::KQueueDatagramChannel,
    ::KQueueEventLoopGroup
  )

  object Epoll : TransportType(
    ::EpollServerSocketChannel,
    ::EpollSocketChannel,
    ::EpollDatagramChannel,
    ::EpollEventLoopGroup
  )

  companion object {

    /**
     * Searches for the best transport type.
     */
    fun findBestType(): TransportType {
      if (System.getProperty("terre.disable-native-transport")?.lowercase() != "true") {
        if (io.netty.channel.kqueue.KQueue.isAvailable())
          return KQueue
        if (io.netty.channel.epoll.Epoll.isAvailable())
          return Epoll
      }
      return Nio
    }
  }
}
