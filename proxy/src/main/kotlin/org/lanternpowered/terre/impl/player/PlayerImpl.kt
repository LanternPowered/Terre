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

import kotlinx.coroutines.Job
import kotlinx.coroutines.future.asDeferred
import org.lanternpowered.terre.MaxPlayers
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerConnectionRequestResult
import org.lanternpowered.terre.event.connection.ClientLoginEvent
import org.lanternpowered.terre.event.connection.ClientPostLoginEvent
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.ServerImpl
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.MultistateProtocol
import org.lanternpowered.terre.impl.network.client.ClientPlayConnectionHandler
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.toDeferred
import org.lanternpowered.terre.impl.text.MessageReceiverImpl
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.util.ColorHue
import java.net.SocketAddress
import java.util.UUID
import java.util.concurrent.CompletableFuture

internal class PlayerImpl(
    val clientConnection: Connection,
    override val protocolVersion: ProtocolVersion,
    val protocol: MultistateProtocol,
    override val name: String,
    override val identifier: PlayerIdentifier,
    override val isMobile: Boolean,
    val uniqueId: UUID
) : Player, MessageReceiverImpl {

  @Volatile override var latency = 0

  override var serverConnection: ServerConnectionImpl? = null
    private set

  override val remoteAddress: SocketAddress
    get() = this.clientConnection.remoteAddress

  /**
   * Whether the player was previously connected to another server.
   */
  var wasPreviouslyConnectedToServer = false

  // Duplicate client UUIDs aren't allowed, however duplicate names are.
  private fun disconnectByDuplicateId() {
    this.clientConnection.close(textOf(
        "There's already a player connected with the identifier: $identifier"))
  }

  fun checkDuplicateIdentifier(): Boolean {
    if (ProxyImpl.mutablePlayers.contains(this.identifier)) {
      disconnectByDuplicateId()
      return true
    }
    return false
  }

  /**
   * Initializes the player and adds it to the proxy.
   */
  fun finishLogin(originalResult: ClientLoginEvent.Result) {
    this.clientConnection.protocol = this.protocol[MultistateProtocol.State.Play]
    if (checkDuplicateIdentifier())
      return

    this.clientConnection.setConnectionHandler(ClientPlayConnectionHandler(this))

    var result = originalResult
    if (result is ClientLoginEvent.Result.Allowed) {
      val maxPlayers = ProxyImpl.maxPlayers
      if (maxPlayers is MaxPlayers.Limited && ProxyImpl.players.size >= maxPlayers.amount) {
        result = ClientLoginEvent.Result.Denied(textOf("The server is full."))
      }
    }

    TerreEventBus.postAsyncWithFuture(ClientLoginEvent(this, result))
        .thenAcceptAsync({ event ->
          if (this.clientConnection.isClosed)
            return@thenAcceptAsync
          if (ProxyImpl.mutablePlayers.addIfAbsent(this) != null) {
            disconnectByDuplicateId()
            return@thenAcceptAsync
          }
          val eventResult = event.result
          if (eventResult is ClientLoginEvent.Result.Denied) {
            this.clientConnection.close(eventResult.reason)
          } else {
            TerreEventBus.postAsyncWithFuture(ClientPostLoginEvent(this))
                .thenAccept { afterLogin() }
          }
        }, this.clientConnection.eventLoop)
  }

  private fun afterLogin() {
    // Try to connect to one of the servers
    val possibleServers = ProxyImpl.servers.asSequence()
        .filter { it.allowAutoJoin }
        .toList()
    connectToAnyWithFuture(possibleServers).whenComplete { connected, _ ->
      if (connected == null) {
        disconnectAndForget(textOf("Failed to connect to a server."))
      }
    }
  }

  private fun connectToAnyWithFuture(servers: Iterable<Server>): CompletableFuture<Server?> {
    val queue = servers.toMutableList()
    if (queue.isEmpty())
      return CompletableFuture.completedFuture(null)

    val future = CompletableFuture<Server?>()

    fun connectNextOrComplete() {
      if (queue.isEmpty()) {
        future.complete(null)
        return
      }
      val next = queue.removeAt(0)
      connectToWithFuture(next).whenComplete { result, _ ->
        if (result is ServerConnectionRequestResult.Success) {
          future.complete(result.server)
        } else {
          connectNextOrComplete()
        }
      }
    }
    connectNextOrComplete()

    return future
  }

  /**
   * Called when the player loses connection
   * to the backing server.
   */
  fun disconnectedFromServer(connection: ServerConnectionImpl) {
    if (this.serverConnection != connection)
      return
    this.serverConnection = null

    val server = connection.server
    // Evacuate the player to another server
    val possibleServers = ProxyImpl.servers.asSequence()
        .filter { it.allowAutoJoin && it != server }
        .toList()
    connectToAnyWithFuture(possibleServers).whenComplete { connected, _ ->
      if (connected == null) {
        disconnectAndForget(textOf("Failed to connect to a server."))
      }
    }
  }

  override fun connectToAnyAsync(servers: Iterable<Server>) = connectToAnyWithFuture(servers).asDeferred()

  fun cleanup() {
    ProxyImpl.mutablePlayers.remove(this)
    this.serverConnection?.connection?.close()
  }

  override fun sendMessage(message: Text) {
    this.clientConnection.send(ChatMessagePacket(message))
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    // If the player is on a server and the sender is also a player and on the same server,
    // send as a player chat message, this will show the text message above the head of the sender.
    if (sender is PlayerImpl) {
      val serverConnection = sender.serverConnection
      if (serverConnection != null && this.serverConnection?.server == serverConnection.server) {
        val playerId = serverConnection.playerId
        if (playerId != null) {
          this.clientConnection.send(PlayerChatMessagePacket(playerId, message))
          return
        }
      }
    }
    super.sendMessageAs(message, sender)
  }

  override fun openPortal(position: Vec2f, colorHue: ColorHue): Portal {
    TODO("Not yet implemented")
  }

  override fun openPortal(position: Vec2f): Portal {
    TODO("Not yet implemented")
  }

  private fun disconnectAndForget(reason: Text) {
    this.clientConnection.close(reason)
    this.serverConnection?.connection?.close()
  }

  override fun disconnectAsync(reason: Text): Job {
    return this.clientConnection.close(reason).toDeferred()
  }

  fun connectToWithFuture(server: Server): CompletableFuture<ServerConnectionRequestResult> {
    server as ServerImpl
    val connection = ServerConnectionImpl(server, this)
    return connection.connect().whenComplete { result, throwable ->
      if (throwable != null) {
        Terre.logger.debug("Failed to establish connection to backend server: ${server.info}", throwable)
      } else if (result is ServerConnectionRequestResult.Success) {
        val old = this.serverConnection?.connection
        if (old != null) {
          old.setConnectionHandler(null)
          old.close()
        }
        // Replace it with the successfully established one
        this.serverConnection = connection
        Terre.logger.debug("Successfully established connection to backend server: ${server.info}")
      }
    }
  }

  override fun connectToAsync(server: Server) = connectToWithFuture(server).asDeferred()
}
