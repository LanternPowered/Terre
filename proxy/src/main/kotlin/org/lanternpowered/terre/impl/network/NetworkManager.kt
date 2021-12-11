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

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelFactory
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.resolver.dns.DnsAddressResolverGroup
import io.netty.resolver.dns.DnsNameResolverBuilder
import org.lanternpowered.terre.impl.network.client.ClientInitConnectionHandler
import org.lanternpowered.terre.impl.network.client.ClientInitProtocol
import org.lanternpowered.terre.impl.network.pipeline.FrameDecoder
import org.lanternpowered.terre.impl.network.pipeline.FrameEncoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageDecoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageEncoder
import java.net.SocketAddress
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

internal val ReadTimeout = 20.seconds
internal val ConnectTimeout = 5.seconds

internal class NetworkManager {

  private val transportType = TransportType.findBestType()

  private val bossGroup = transportType
    .eventLoopGroupSupplier(0, NettyThreadFactory("boss"))

  private val workerGroup = transportType
    .eventLoopGroupSupplier(0, NettyThreadFactory("worker"))

  private val resolverGroup = DnsAddressResolverGroup(
    DnsNameResolverBuilder()
      .channelFactory(transportType.datagramChannelSupplier)
      .negativeTtl(15)
      .ndots(1))

  private var endpoint: Channel? = null

  fun bind(address: SocketAddress): ChannelFuture {
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
    }.bind(address).addChannelFutureListener { future -> // SAM constructor isn't redundant
      this.endpoint = future.channel()
    }
  }

  private fun initChannel(channel: SocketChannel) {
    val connection = Connection(channel)
    connection.protocol = ClientInitProtocol
    connection.setConnectionHandler(ClientInitConnectionHandler(connection))
    val pipeline = channel.pipeline()
    pipeline.apply {
      addLast(ReadTimeoutHandler(ReadTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS))
      addLast(FrameDecoder())
      addLast(FrameEncoder())
      addLast(PacketMessageDecoder(
        PacketCodecContextImpl(connection, PacketDirection.ClientToServer)))
      addLast(PacketMessageEncoder(
        PacketCodecContextImpl(connection, PacketDirection.ServerToClient)))
      addLast(connection)
    }
  }

  fun shutdown() {
    // Close the endpoint to prevent any new connections
    endpoint?.close()?.sync()
  }

  /**
   * Creates a client bootstrap.
   */
  fun createClientBootstrap(group: EventLoopGroup = workerGroup): Bootstrap {
    return Bootstrap().apply {
      channelFactory(ChannelFactory(transportType.socketChannelSupplier))
      group(group)
      option(ChannelOption.TCP_NODELAY, true)
      option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ConnectTimeout.toInt(DurationUnit.MILLISECONDS))
      resolver(resolverGroup)
    }
  }
}
