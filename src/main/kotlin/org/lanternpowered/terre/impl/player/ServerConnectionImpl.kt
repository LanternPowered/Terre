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
import io.netty.handler.timeout.ReadTimeoutHandler
import org.lanternpowered.terre.ConnectionRequestResult
import org.lanternpowered.terre.ServerConnection
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.ServerImpl
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.PacketCodecContextImpl
import org.lanternpowered.terre.impl.network.PacketDirection
import org.lanternpowered.terre.impl.network.ReadTimeout
import org.lanternpowered.terre.impl.network.addChannelFutureListener
import org.lanternpowered.terre.impl.network.backend.ServerInitConnectionHandler
import org.lanternpowered.terre.impl.network.backend.ServerInitConnectionResult
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

  fun connect(): CompletableFuture<ConnectionRequestResult> {
    val result = CompletableFuture<ConnectionRequestResult>()
    val connected = this.player.serverConnection
    if (connected != null && connected.server == this.server) {
      result.complete(ConnectionRequestResult.AlreadyConnected(this.server))
      return result
    }
    val bootstrap = ProxyImpl.networkManager
        .createClientBootstrap(this.player.clientConnection.eventLoop)
    val connectFuture = bootstrap
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

  private fun Channel.init(resultFuture: CompletableFuture<ConnectionRequestResult>) {
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
      } else {
        if (result is ConnectionRequestResult.Success)
          this@ServerConnectionImpl.playerId = playerId
        resultFuture.complete(result)
      }
    }
    // TODO: Support for modded
    // TODO: Retry a connection if packet translation is supported
    //  for specific versions, but with a different protocol version.
    connection.setConnectionHandler(ServerInitConnectionHandler(this@ServerConnectionImpl, future,
        listOf(ProtocolVersion.Vanilla(player.clientConnection.protocol.version))))
  }

  fun ensureConnected(): Connection {
    return this.connection ?: error("Not connected!")
  }
}
