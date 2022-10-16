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
import org.lanternpowered.terre.impl.network.MultistateProtocol
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
import org.lanternpowered.terre.text.textOf
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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

  /**
   * Whether the world is initialized.
   */
  var isWorldInitialized: Boolean = false

  init {
    if (!player.wasPreviouslyConnectedToServer)
      isWorldInitialized = true
  }

  fun connect(): CompletableFuture<ServerConnectionRequestResult> {
    val future = CompletableFuture<ServerConnectionRequestResult>()

    // Unregistered servers shouldn't be connected to anymore
    if (server.unregistered) {
      future.completeExceptionally(IllegalArgumentException("The server \"$server\" is unregistered."))
      return future
    }

    // No need to reconnect to the same server
    val connected = player.serverConnection
    if (connected != null && connected.server == server) {
      future.complete(ServerConnectionRequestResult.AlreadyConnected(server))
      return future
    }

    val clientProtocol = player.protocol
    // Check if there's a fixed version that should be used, otherwise try every possible
    // protocol version.
    val versionedProtocol = server.versionedProtocol
    val versionsToAttempt = if (versionedProtocol != null) {
      mutableListOf(versionedProtocol)
    } else {
      ProtocolRegistry.allowedTranslations.asSequence()
        .filter { translation -> translation.from == clientProtocol }
        .flatMap { translation ->
          ProtocolRegistry.all.asSequence().filter { it.protocol == translation.to }
        }
        .let {
          // Prioritize the last known entry, for faster connections
          val lastKnownVersion = server.lastKnownVersion
          if (lastKnownVersion != null) {
            it.sortedWith { o1, _ ->
              if (o1.version == lastKnownVersion) -1 else 0
            }
          } else it
        }
        .toMutableList()
    }

    var firstThrowable: Throwable? = null

    fun tryConnectNext() {
      if (player.clientConnection.isClosed) {
        future.complete(ServerConnectionRequestResult.Disconnected(
          server, textOf("Client already disconnected.")))
        return
      }
      val (version, protocol) = versionsToAttempt.removeAt(0)
      connect(protocol, version).whenComplete { result, throwable ->
        if (throwable == null) {
          if (result is ServerInitConnectionResult.Success) {
            future.complete(ServerConnectionRequestResult.Success(server))
          } else {
            result as ServerInitConnectionResult.Disconnected
            if (versionsToAttempt.isEmpty()) {
              future.complete(ServerConnectionRequestResult.Disconnected(server, result.reason))
            } else {
              tryConnectNext()
            }
          }
        } else {
          if (firstThrowable == null)
            firstThrowable = throwable
          if (versionsToAttempt.isEmpty()) {
            future.completeExceptionally(firstThrowable)
          } else {
            tryConnectNext()
          }
        }
      }
    }

    tryConnectNext()
    return future
  }

  private fun connect(
    protocol: MultistateProtocol, version: ProtocolVersion
  ): CompletableFuture<ServerInitConnectionResult> {
    val result = CompletableFuture<ServerInitConnectionResult>()
    ProxyImpl.networkManager
      .createClientBootstrap(player.clientConnection.eventLoop)
      // There must be a handler, otherwise connect just freezes
      .handler(object : ChannelInitializer<Channel>() {
        override fun initChannel(channel: Channel) {}
      })
      .connect(server.info.address)
      .addChannelFutureListener { future ->
        if (future.isSuccess) {
          future.channel().init(protocol, version, result)
        } else {
          result.completeExceptionally(future.cause())
        }
      }
    return result
  }

  private fun Channel.init(
    protocol: MultistateProtocol,
    version: ProtocolVersion,
    future: CompletableFuture<ServerInitConnectionResult>
  ) {
    val connection = Connection(this)
    pipeline().apply {
      addLast(ReadTimeoutHandler(ReadTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS))
      addLast(FrameDecoder())
      addLast(FrameEncoder())
      addLast(PacketMessageDecoder(PacketCodecContextImpl(connection, PacketDirection.ServerToClient)))
      addLast(PacketMessageEncoder(PacketCodecContextImpl(connection, PacketDirection.ClientToServer)))
      addLast(connection)
    }
    future.whenComplete { result, throwable ->
      if (throwable != null || result !is ServerInitConnectionResult.Success) {
        connection.close()
        return@whenComplete
      }
      // Store the version, so other connections to this
      // server can be made faster.
      server.lastKnownVersion = version
      this@ServerConnectionImpl.playerId = result.playerId
      this@ServerConnectionImpl.connection = connection
      Terre.logger.debug { "Successfully made a new connection to ${server.info}" }
      // Continue server connection after it has been approved
      connection.setConnectionHandler(ServerPlayConnectionHandler(
        this@ServerConnectionImpl, player))
    }
    connection.setConnectionHandler(ServerInitConnectionHandler(
      connection, player.clientConnection, future, version, protocol, server.info.password))
  }

  fun ensureConnected(): Connection {
    return this.connection ?: error("Not connected!")
  }
}
