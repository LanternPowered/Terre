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
import kotlinx.coroutines.asCoroutineDispatcher
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import java.io.IOException
import java.net.SocketAddress
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

internal class Connection(
  private val channel: Channel
) : ChannelInboundHandlerAdapter() {

  private var disconnectReason: Text? = null
  private var connectionHandler: ConnectionHandler? = null

  /**
   * The current protocol.
   */
  lateinit var protocol: Protocol

  /**
   * Sets the current [ConnectionHandler].
   */
  fun setConnectionHandler(connectionHandler: ConnectionHandler?) {
    this.connectionHandler = connectionHandler
    connectionHandler?.initialize()
  }

  /**
   * The event loop of the connection.
   */
  val eventLoop: EventLoop
    get() = channel.eventLoop()

  /**
   * The coroutine dispatcher.
   */
  val coroutineDispatcher = channel.eventLoop().asCoroutineDispatcher()

  /**
   * The byte buf allocator of the connection.
   */
  val byteBufAlloc: ByteBufAllocator
    get() = channel.alloc()

  /**
   * The remote address this connection is connected to.
   */
  val remoteAddress: SocketAddress
      get() = channel.remoteAddress()

  /**
   * The local address of this connection.
   */
  val localAddress: SocketAddress
    get() = channel.localAddress()

  /**
   * Whether the connection is open.
   */
  val isOpen: Boolean
    get() = channel.isOpen

  /**
   * Whether the connection is closed.
   */
  val isClosed: Boolean
    get() = !channel.isOpen

  /**
   * Whether this connection uses mobile.
   */
  var nonePlayerId: PlayerId = PlayerId.None

  /**
   * Closes the connection.
   */
  fun close(): ChannelFuture {
    return channel.close()
  }

  /**
   * Closes the connection with the given reason.
   *
   * @param reason The reason
   */
  fun close(reason: Text): ChannelFuture = close(reason) { DisconnectPacket(reason) }

  /**
   * Closes the connection with the given reason.
   *
   * @param reason The reason
   * @param packet The packet constructor
   */
  fun close(reason: Text, packet: () -> Packet): ChannelFuture {
    if (disconnectReason != null)
      return channel.newSucceededFuture()
    val promise = channel.newPromise()
    channel.eventLoop().execute {
      // Is already disconnected
      if (disconnectReason != null) {
        promise.setSuccess()
        return@execute
      }
      disconnectReason = reason
      sendWithFuture(packet(), promise)
        .addListener(ChannelFutureListener.CLOSE)
    }
    return promise
  }

  override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
    val connectionHandler = connectionHandler
    if (connectionHandler == null) {
      ReferenceCountUtil.release(packet)
      return
    }
    var release = true
    try {
      if (packet is Packet) {
        val binding = ConnectionHandlerBindings.getBinding(packet.javaClass)
        if (binding != null) {
          if (binding is ConnectionHandlerBindings.SimpleBinding<Packet>) {
            if (!binding.handle(connectionHandler, packet))
              connectionHandler.handleGeneric(packet)
          } else {
            binding as ConnectionHandlerBindings.SuspendBinding<Packet>
            val function = suspend {
              binding.handle(connectionHandler, packet)
            }
            release = false
            function.startCoroutine(Continuation(EmptyCoroutineContext) { result ->
              if (result.getOrNull() == false) {
                val task = Runnable {
                  try {
                    connectionHandler.handleGeneric(packet)
                  } finally {
                    ReferenceCountUtil.release(packet)
                  }
                }
                if (eventLoop.inEventLoop()) {
                  task.run()
                } else {
                  eventLoop.execute(task)
                }
              } else {
                ReferenceCountUtil.release(packet)
                val exception = result.exceptionOrNull()
                if (exception != null)
                  ctx.fireExceptionCaught(exception)
              }
            })
          }
        } else {
          connectionHandler.handleGeneric(packet)
        }
      } else if (packet is ByteBuf) {
        connectionHandler.handleUnknown(packet)
      }
    } finally {
      if (release)
        ReferenceCountUtil.release(packet)
    }
  }

  override fun channelActive(ctx: ChannelHandlerContext) {
  }

  override fun channelInactive(ctx: ChannelHandlerContext) {
    connectionHandler?.disconnect()
    // The player probably just left the server
    if (disconnectReason == null) {
      disconnectReason = if (channel.isOpen) {
        textOf("End of stream")
      } else {
        textOf("Disconnected")
      }
    }
  }

  @Deprecated("Not deprecated")
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    connectionHandler?.exception(cause)
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
    disconnectReason = reason
    channel.close()
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
    if (!channel.isActive)
      return promise
    ReferenceCountUtil.retain(packet)
    // Write the packet and add a exception handler
    return channel.writeAndFlush(packet, promise)
      .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  fun sendWithFuture(packets: Array<out ByteBuf>): ChannelFuture {
    return sendWithFuture(packets)
  }

  fun sendWithFuture(packets: Array<out Packet>): ChannelFuture {
    return sendWithFuture(packets)
  }

  private fun sendWithFuture(packets: Array<*>): ChannelFuture {
    val promise = channel.newPromise()
    if (!channel.isActive)
      return promise
    val eventLoop = channel.eventLoop()
    if (eventLoop.inEventLoop()) {
      channel.writeArrayAndFlushWithFuture(packets, promise)
    } else {
      // Create a copy to avoid unsafe modifications
      val copy = packets.clone()
      eventLoop.execute {
        channel.writeArrayAndFlushWithFuture(copy, promise)
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
    if (!channel.isActive)
      return
    ReferenceCountUtil.retain(packet)
    channel.writeAndFlush(packet, channel.voidPromise())
  }

  fun send(packets: Array<out ByteBuf>) {
    send(packets as Array<*>)
  }

  fun send(packets: Array<out Packet>) {
    send(packets as Array<*>)
  }

  private fun send(packets: Array<*>) {
    if (!channel.isActive)
      return
    val eventLoop = channel.eventLoop()
    if (eventLoop.inEventLoop()) {
      channel.writeArrayAndFlush(packets)
    } else {
      // Create a copy to avoid unsafe modifications
      val copy = packets.clone()
      eventLoop.execute {
        channel.writeArrayAndFlush(copy)
      }
    }
  }

  private inline fun Channel.writeArrayAndFlushWithFuture(
    packets: Array<*>, promise: ChannelPromise
  ) {
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

  private inline fun Channel.writeArrayAndFlush(packets: Array<*>) {
    val voidPromise = voidPromise()
    for (packet in packets) {
      ReferenceCountUtil.retain(packet)
      write(packet, voidPromise)
    }
    flush()
  }

  fun <T> attr(key: AttributeKey<T>): Attribute<T> = channel.attr(key)
}
