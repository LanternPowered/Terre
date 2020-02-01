/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NOTHING_TO_INLINE")

package org.lanternpowered.terre.impl.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.channel.EventLoop
import io.netty.handler.codec.CodecException
import io.netty.handler.timeout.TimeoutException
import io.netty.util.Attribute
import io.netty.util.AttributeKey
import io.netty.util.ReferenceCountUtil
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.KeepAlivePacket
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import java.io.IOException
import java.net.SocketAddress

internal class Connection(
    private val channel: Channel
) : ChannelInboundHandlerAdapter() {

  private var disconnectReason: Text? = null
  private var currentProtocol: Protocol = InitProtocol
  private var connectionHandler: ConnectionHandler? = null

  /**
   * The current protocol.
   */
  val protocol: Protocol
    get() = this.currentProtocol

  /**
   * Sets the current [ConnectionHandler].
   */
  fun setConnectionHandler(connectionHandler: ConnectionHandler) {
    this.connectionHandler = connectionHandler
    connectionHandler.initialize()
  }

  /**
   * Initializes the protocol version.
   */
  fun initProtocol(protocol: Protocol) {
    check(this.currentProtocol == InitProtocol) {
      "Protocol is already initialized." }
    this.currentProtocol = protocol
  }

  /**
   * The event loop of the connection.
   */
  val eventLoop: EventLoop
    get() = this.channel.eventLoop()

  /**
   * The byte buf allocator of the connection.
   */
  val byteBufAlloc: ByteBufAllocator
    get() = this.channel.alloc()

  /**
   * The remote address this connection is connected to.
   */
  val remoteAddress: SocketAddress
      get() = this.channel.remoteAddress()

  /**
   * Closes the connection.
   */
  fun close(): ChannelFuture {
    return this.channel.close()
  }

  /**
   * Closes the connection with the given reason.
   *
   * @param reason The reason
   */
  fun close(reason: Text): ChannelFuture {
    if (this.disconnectReason != null) {
      return this.channel.newSucceededFuture()
    }
    val promise = this.channel.newPromise()
    this.channel.eventLoop().execute {
      // Is already disconnected
      if (this.disconnectReason != null) {
        promise.setSuccess()
        return@execute
      }
      this.disconnectReason = reason
      sendWithFuture(DisconnectPacket(reason), promise)
          .addListener(ChannelFutureListener.CLOSE)
    }
    return promise
  }

  override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
    val connectionHandler = this.connectionHandler ?: return
    try {
      if (packet is Packet) {
        if (packet !is KeepAlivePacket)
          Terre.logger.info("Received packet: $packet")

        val handler = ConnectionHandlerBindings.getHandler(packet.javaClass)
        if (handler != null) {
          if (!handler(connectionHandler, packet)) {
            connectionHandler.handleGeneric(packet)
          }
        } else {
          connectionHandler.handleGeneric(packet)
        }
      } else if (packet is ByteBuf) {
        connectionHandler.handleUnknown(packet)
      }
    } finally {
      ReferenceCountUtil.release(packet)
    }
  }

  override fun channelActive(ctx: ChannelHandlerContext) {
    Terre.logger.info("Client connected from: $remoteAddress")
  }

  override fun channelInactive(ctx: ChannelHandlerContext) {
    this.connectionHandler?.disconnect()
    Terre.logger.info("Client disconnected from: $remoteAddress")
    // The player probably just left the server
    if (this.disconnectReason == null) {
      if (this.channel.isOpen) {
        this.disconnectReason = textOf("End of stream")
      } else {
        this.disconnectReason = textOf("Disconnected")
      }
    }
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    // Pipeline error, just log it
    if (cause is CodecException) {
      Terre.logger.error("A netty pipeline error occurred", cause)
    } else {
      if (cause is IOException) {
        val stack = cause.getStackTrace()
        if (stack.isNotEmpty() && stack[0].toString().startsWith(
                "sun.nio.ch.SocketDispatcher.read0")) {
          return
        }
      }

      // Use the debug level, don't spam the server with errors
      // caused by client disconnection, ...
      Terre.logger.debug("A netty connection error occurred", cause)

      if (cause is TimeoutException) {
        closeChannel(textOf("Timed out"))
      } else {
        closeChannel(textOf("Internal Exception: $cause"))
      }
    }
  }

  /**
   * Closes the channel with a specific disconnect reason, this doesn't
   * send a disconnect message to the client, it just closes the connection.
   *
   * @param reason The reason
   */
  private fun closeChannel(reason: Text) {
    this.disconnectReason = reason
    this.channel.close()
  }

  fun sendWithFuture(packet: ByteBuf): ChannelFuture {
    return sendWithFuture(packet as Any)
  }

  fun sendWithFuture(packet: Packet): ChannelFuture {
    return sendWithFuture(packet as Any)
  }

  private fun sendWithFuture(packet: Any): ChannelFuture {
    return sendWithFuture(packet, channel.newPromise())
  }

  fun sendWithFuture(packet: ByteBuf, promise: ChannelPromise): ChannelFuture {
    return sendWithFuture(packet as Any, promise)
  }

  fun sendWithFuture(packet: Packet, promise: ChannelPromise): ChannelFuture {
    return sendWithFuture(packet as Any, promise)
  }

  private fun sendWithFuture(packet: Any, promise: ChannelPromise): ChannelFuture {
    if (!this.channel.isActive) {
      return promise
    }
    ReferenceCountUtil.retain(packet)
    // Write the packet and add a exception handler
    return this.channel.writeAndFlush(packet, promise)
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  fun sendWithFuture(packets: Array<out ByteBuf>): ChannelFuture {
    return sendWithFuture(packets)
  }

  fun sendWithFuture(packets: Array<out Packet>): ChannelFuture {
    return sendWithFuture(packets)
  }

  private fun sendWithFuture(packets: Array<*>): ChannelFuture {
    val promise = this.channel.newPromise()
    if (!this.channel.isActive) {
      return promise
    }
    val eventLoop = this.channel.eventLoop()
    if (eventLoop.inEventLoop()) {
      this.channel.writeArrayAndFlushWithFuture(packets, promise)
    } else {
      // Create a copy to avoid unsafe modifications
      val copy = packets.clone()
      eventLoop.execute {
        this.channel.writeArrayAndFlushWithFuture(copy, promise)
      }
    }
    return promise
  }

  fun send(packet: ByteBuf) {
    send(packet as Any)
  }

  fun send(packet: Packet) {
    send(packet as Any)
  }

  private fun send(packet: Any) {
    if (!this.channel.isActive)
      return
    ReferenceCountUtil.retain(packet)
    this.channel.writeAndFlush(packet, this.channel.voidPromise())
  }

  fun send(packets: Array<out ByteBuf>) {
    send(packets)
  }

  fun send(packets: Array<out Packet>) {
    send(packets)
  }

  private fun send(packets: Array<*>) {
    if (!this.channel.isActive)
      return
    val eventLoop = this.channel.eventLoop()
    if (eventLoop.inEventLoop()) {
      this.channel.writeArrayAndFlush(packets)
    } else {
      // Create a copy to avoid unsafe modifications
      val copy = packets.clone()
      eventLoop.execute {
        this.channel.writeArrayAndFlush(copy)
      }
    }
  }

  private inline fun Channel.writeArrayAndFlushWithFuture(
      packets: Array<*>, promise: ChannelPromise) {
    val voidPromise = voidPromise()
    for (i in packets.indices) {
      val packet = packets[i]
      ReferenceCountUtil.retain(packet)
      if (i == packets.size - 1) {
        writeAndFlush(packet, promise)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
      } else {
        write(packet, voidPromise)
      }
    }
  }

  private inline fun Channel.writeArrayAndFlush(
      packets: Array<*>) {
    val voidPromise = voidPromise()
    for (packet in packets) {
      ReferenceCountUtil.retain(packet)
      write(packet, voidPromise)
    }
    flush()
  }

  fun <T> attr(key: AttributeKey<T>): Attribute<T> = this.channel.attr(key)
}
