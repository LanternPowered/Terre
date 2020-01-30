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

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelFactory
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import org.lanternpowered.terre.impl.network.client.ClientInitConnectionHandler
import org.lanternpowered.terre.impl.network.pipeline.FrameDecoder
import org.lanternpowered.terre.impl.network.pipeline.FrameEncoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageDecoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageEncoder
import java.net.SocketAddress

private const val ReadTimeoutSeconds = 20

internal class NetworkManager {

  private val transportType = TransportType.findBestType()

  val bossGroup: EventLoopGroup = this.transportType.eventLoopGroupSupplier(0, NettyThreadFactory("boss"))
  val workerGroup: EventLoopGroup = this.transportType.eventLoopGroupSupplier(0, NettyThreadFactory("worker"))

  private var endpoint: Channel? = null

  fun init(address: SocketAddress): ChannelFuture {
    return ServerBootstrap().apply {
      group(bossGroup, workerGroup)
      channelFactory(ChannelFactory(transportType.serverSocketChannelSupplier))
      childHandler(object : ChannelInitializer<SocketChannel>() {
        override fun initChannel(channel: SocketChannel) = this@NetworkManager.initChannel(channel)
      })
      childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
      childOption(ChannelOption.TCP_NODELAY, true)
      childOption(ChannelOption.IP_TOS, 0x18)
      childOption(ChannelOption.SO_KEEPALIVE, true)
    }.bind(address).addListener(ChannelFutureListener { future -> // SAM constructor isn't redundant
      this.endpoint = future.channel()
    })
  }

  private fun initChannel(channel: SocketChannel) {
    val connection = Connection(this, channel)
    connection.setConnectionHandler(ClientInitConnectionHandler(connection))
    val pipeline = channel.pipeline()
    pipeline.apply {
      addLast(ReadTimeoutHandler(ReadTimeoutSeconds))
      addLast(FrameDecoder())
      addLast(FrameEncoder())
      addLast(PacketMessageDecoder(PacketCodecContextImpl(connection, channel, PacketDirection.ClientToServer)))
      addLast(PacketMessageEncoder(PacketCodecContextImpl(connection, channel, PacketDirection.ServerToClient)))
      addLast(connection)
    }
  }

  fun shutdown() {
    // Close the endpoint to prevent any new connections
    this.endpoint?.close()?.sync()
  }

  private class PacketCodecContextImpl(
      override val connection: Connection,
      override val channel: Channel,
      override val direction: PacketDirection
  ) : PacketCodecContext {
    override val protocol: Protocol get() = this.connection.protocol
    override val byteBufAllocator: ByteBufAllocator get() = this.channel.alloc()
  }
}
