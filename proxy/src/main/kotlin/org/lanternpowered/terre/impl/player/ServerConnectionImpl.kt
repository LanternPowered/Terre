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
import org.lanternpowered.terre.impl.network.VersionedProtocol
import org.lanternpowered.terre.impl.network.addChannelFutureListener
import org.lanternpowered.terre.impl.network.backend.ServerInitConnectionHandler
import org.lanternpowered.terre.impl.network.backend.ServerInitConnectionResult
import org.lanternpowered.terre.impl.network.backend.ServerPlayConnectionHandler
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.pipeline.FrameDecoder
import org.lanternpowered.terre.impl.network.pipeline.FrameEncoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageDecoder
import org.lanternpowered.terre.impl.network.pipeline.PacketMessageEncoder
import org.lanternpowered.terre.text.textOf
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

internal class ServerConnectionImpl(
  override val server: ServerImpl,
  override val player: PlayerImpl,
) : ServerConnection {

  private var nullablePlayerId: PlayerId? = null

  /**
   * The id that the player got assigned by the server.
   */
  val playerId: PlayerId
    get() = nullablePlayerId ?: error("playerId not initialized")

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

  /**
   * Accepts the successful connection.
   */
  fun accept() {
    val connection = connection ?: error("No connection to accept.")
    // Continue server connection after it has been approved
    connection.setConnectionHandler(ServerPlayConnectionHandler(
      this@ServerConnectionImpl, player))
    // Sending this packet triggers the client to request all the information from the
    // server once again, this allows it to request and load a new world.
    player.clientConnection.send(ConnectionApprovedPacket(playerId))
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
        .distinct()
        .let {
          // Prioritize the last known entry, for faster connections
          val lastKnownVersion = server.lastKnownVersion
          var comparator = Comparator<VersionedProtocol> { o1, o2 ->
            val v1 = o1.version
            val v2 = o2.version
            when {
              v1 is ProtocolVersion.Vanilla && v2 is ProtocolVersion.Vanilla -> -v1.compareTo(v2)
              else -> 0
            }
          }
          if (lastKnownVersion != null) {
            comparator = Comparator<VersionedProtocol> { o1, _ ->
              if (o1.version == lastKnownVersion) -1 else 0
            }.thenComparing(comparator)
          }
          it.sortedWith(comparator)
        }
        .toMutableList()
    }

    var firstThrowable: Throwable? = null

    fun tryConnect(versionedProtocol: VersionedProtocol, modded: Boolean) {
      if (player.clientConnection.isClosed) {
        future.complete(ServerConnectionRequestResult.Disconnected(
          server, textOf("Client already disconnected.")))
        return
      }
      connect(versionedProtocol, modded).whenComplete { result, throwable ->
        if (throwable == null) {
          when (result) {
            is ServerInitConnectionResult.Success -> {
              server.modded = modded
              future.complete(ServerConnectionRequestResult.Success(server))
            }
            is ServerInitConnectionResult.Disconnected -> {
              future.complete(ServerConnectionRequestResult.Disconnected(server, result.reason))
            }
            is ServerInitConnectionResult.UnsupportedProtocol -> {
              if (versionsToAttempt.isEmpty()) {
                future.complete(ServerConnectionRequestResult.Disconnected(server, result.reason))
              } else {
                tryConnect(versionsToAttempt.removeAt(0), modded)
              }
            }
            is ServerInitConnectionResult.NotModded -> {
              tryConnect(versionedProtocol, false)
            }
          }
        } else {
          if (firstThrowable == null)
            firstThrowable = throwable
          if (versionsToAttempt.isEmpty()) {
            future.completeExceptionally(firstThrowable)
          } else {
            tryConnect(versionsToAttempt.removeAt(0), modded)
          }
        }
      }
    }

    tryConnect(versionsToAttempt.removeAt(0), server.modded ?: true)
    return future
  }

  private fun connect(
    versionedProtocol: VersionedProtocol,
    modded: Boolean,
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
          future.channel().init(versionedProtocol, result, modded)
        } else {
          result.completeExceptionally(future.cause())
        }
      }
    return result
  }

  private fun Channel.init(
    versionedProtocol: VersionedProtocol,
    future: CompletableFuture<ServerInitConnectionResult>,
    modded: Boolean,
  ) {
    val connection = Connection(this)
    pipeline().apply {
      addLast(ReadTimeoutHandler(ReadTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS))
      addLast(FrameDecoder())
      addLast(FrameEncoder())
      addLast(PacketMessageDecoder(PacketCodecContextImpl(connection, PacketDirection.ServerToClient)))
      addLast(PacketMessageEncoder(PacketCodecContextImpl(connection, PacketDirection.ClientToServer)))
      addLast(connection)
      addLast(connection.outboundHandler)
    }
    future.whenComplete { result, throwable ->
      if (throwable != null || result !is ServerInitConnectionResult.Success) {
        connection.close()
        return@whenComplete
      }
      // Store the version, so other connections to this server can be made faster.
      server.lastKnownVersion = versionedProtocol.version
      this@ServerConnectionImpl.nullablePlayerId = result.playerId
      this@ServerConnectionImpl.connection = connection
      Terre.logger.debug { "Successfully made a new connection to ${server.info}" }
    }
    val clientIP = player.remoteAddress.address.hostAddress
    val playerInfo = player.lastPlayerInfo
    val password = server.info.password
    connection.setConnectionHandler(ServerInitConnectionHandler(
      connection, future, versionedProtocol, password, playerInfo, modded, clientIP))
  }

  fun ensureConnected(): Connection {
    return this.connection ?: error("Not connected!")
  }
}
