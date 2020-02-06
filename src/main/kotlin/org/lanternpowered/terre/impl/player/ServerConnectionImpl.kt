/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.player

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.timeout.ReadTimeoutHandler
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.ServerConnection
import org.lanternpowered.terre.ServerConnectionRequestResult
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.ServerImpl
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.PacketCodecContextImpl
import org.lanternpowered.terre.impl.network.PacketDirection
import org.lanternpowered.terre.impl.network.ProtocolRegistry
import org.lanternpowered.terre.impl.network.ReadTimeout
import org.lanternpowered.terre.impl.network.addChannelFutureListener
import org.lanternpowered.terre.impl.network.backend.ServerInitConnectionHandler
import org.lanternpowered.terre.impl.network.backend.ServerInitConnectionResult
import org.lanternpowered.terre.impl.network.backend.ServerPlayConnectionHandler
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.pipeline.FrameDecoder
import org.lanternpowered.terre.impl.network.pipeline.FrameEncoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageDecoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageEncoder
import java.util.concurrent.CompletableFuture
import kotlin.time.DurationUnit

internal class ServerConnectionImpl(
    override val server: ServerImpl,
    override val player: PlayerImpl
) : ServerConnection {

  /**
   * The id that the player got assigned by the server.
   */
  var playerId: PlayerId? = null
    private set

  var connection: Connection? = null
    private set

  fun connect(): CompletableFuture<ServerConnectionRequestResult> {
    val result = CompletableFuture<ServerConnectionRequestResult>()
    if (this.server.unregistered) {
      result.completeExceptionally(IllegalArgumentException("The server \"$server\" is unregistered."))
      return result
    }
    val connected = this.player.serverConnection
    if (connected != null && connected.server == this.server) {
      result.complete(ServerConnectionRequestResult.AlreadyConnected(this.server))
      return result
    }
    val bootstrap = ProxyImpl.networkManager
        .createClientBootstrap(this.player.clientConnection.eventLoop)
    val connectFuture = bootstrap
        // There must be a handler, otherwise connect just freezes
        .handler(object : ChannelInitializer<Channel>() {
          override fun initChannel(channel: Channel) {}
        })
        .connect(this.server.info.address)
    connectFuture.addChannelFutureListener { future ->
      if (future.isSuccess) {
        future.channel().init(result)
      } else {
        result.completeExceptionally(future.cause())
      }
    }
    return result
  }

  private fun Channel.init(resultFuture: CompletableFuture<ServerConnectionRequestResult>) {
    Terre.logger.debug("P -> S(${server.info.name}) [${player.name}] Connection established.")
    val connection = Connection(this)
    pipeline().apply {
      addLast(ReadTimeoutHandler(ReadTimeout.toLongMilliseconds(), DurationUnit.MILLISECONDS))
      addLast(FrameDecoder())
      addLast(FrameEncoder())
      addLast(PacketMessageDecoder(PacketCodecContextImpl(connection, PacketDirection.ServerToClient)))
      addLast(PacketMessageEncoder(PacketCodecContextImpl(connection, PacketDirection.ClientToServer)))
      addLast(connection)
    }
    this@ServerConnectionImpl.connection = connection
    val future = CompletableFuture<ServerInitConnectionResult>()
    future.whenComplete { (result, playerId), throwable ->
      if (throwable != null) {
        resultFuture.completeExceptionally(throwable)
        connection.close()
      } else {
        if (result is ServerConnectionRequestResult.Success) {
          this@ServerConnectionImpl.playerId = playerId
          afterConnectionApproved()
        } else {
          connection.close()
        }
        resultFuture.complete(result)
      }
    }
    val protocol = player.clientConnection.protocol
    val versionsToAttempt = ProtocolRegistry.all.map { it to ProtocolVersion.Vanilla(it.version) } +
        ProtocolRegistry.allowedTranslations.asSequence()
            .filter { it.from == protocol }
            .map { it.to to ProtocolVersion.Vanilla(it.to.version) }
            .toList()

    // TODO: Support for modded
    connection.setConnectionHandler(ServerInitConnectionHandler(
        this@ServerConnectionImpl, future, versionsToAttempt))
  }

  private fun afterConnectionApproved() {
    // Continue server connection after it has been approved
    ensureConnected().setConnectionHandler(
        ServerPlayConnectionHandler(this, this.player))
  }

  fun ensureConnected(): Connection {
    return this.connection ?: error("Not connected!")
  }
}
