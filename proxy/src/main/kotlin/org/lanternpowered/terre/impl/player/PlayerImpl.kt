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
import org.lanternpowered.terre.impl.network.buffer.NpcType
import org.lanternpowered.terre.impl.network.client.ClientPlayConnectionHandler
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdatePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.SimpleItemUpdatePacket
import org.lanternpowered.terre.impl.network.toDeferred
import org.lanternpowered.terre.impl.network.tracking.TrackedItems
import org.lanternpowered.terre.impl.network.tracking.TrackedNpcs
import org.lanternpowered.terre.impl.network.tracking.TrackedPlayers
import org.lanternpowered.terre.impl.network.tracking.TrackedProjectiles
import org.lanternpowered.terre.impl.text.MessageReceiverImpl
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.portal.PortalBuilder
import org.lanternpowered.terre.portal.PortalType
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.util.AABB
import java.net.SocketAddress
import java.util.UUID
import java.util.concurrent.CompletableFuture

internal class PlayerImpl(
  val clientConnection: Connection,
  override val protocolVersion: ProtocolVersion,
  val protocol: MultistateProtocol,
  override val name: String,
  override val identifier: PlayerIdentifier,
  val uniqueId: UUID
) : Player, MessageReceiverImpl {

  @Volatile override var latency = 0

  override var serverConnection: ServerConnectionImpl? = null
    private set

  override val remoteAddress: SocketAddress
    get() = clientConnection.remoteAddress

  /**
   * The team this player is currently part of, or white if none.
   */
  var team = Team.White

  @Volatile override var position: Vec2f = Vec2f.Zero

  override val boundingBox: AABB
    get() = AABB.centerSize(Vec2f(20f, 42f)).offset(position)

  /**
   * The NPCs this player is aware of.
   */
  val trackedNpcs = TrackedNpcs()

  /**
   * The players this player is aware of.
   */
  val trackedPlayers = TrackedPlayers()

  /**
   * The items this player is aware of.
   */
  val trackedItems = TrackedItems()

  /**
   * The projectiles this player is aware of.
   */
  val trackedProjectiles = TrackedProjectiles()

  /**
   * Whether the player was previously connected to another server.
   */
  var wasPreviouslyConnectedToServer = false

  // Duplicate client UUIDs aren't allowed, however duplicate names are.
  private fun disconnectByDuplicateId() {
    clientConnection.close(textOf(
      "There's already a player connected with the identifier: $identifier"))
  }

  fun checkDuplicateIdentifier(): Boolean {
    if (ProxyImpl.mutablePlayers.contains(identifier)) {
      disconnectByDuplicateId()
      return true
    }
    return false
  }

  /**
   * Initializes the player and adds it to the proxy.
   */
  fun finishLogin(originalResult: ClientLoginEvent.Result) {
    clientConnection.protocol = protocol[MultistateProtocol.State.Play]
    if (checkDuplicateIdentifier())
      return

    clientConnection.setConnectionHandler(ClientPlayConnectionHandler(this))

    var result = originalResult
    if (result is ClientLoginEvent.Result.Allowed) {
      val maxPlayers = ProxyImpl.maxPlayers
      if (maxPlayers is MaxPlayers.Limited && ProxyImpl.players.size >= maxPlayers.amount) {
        result = ClientLoginEvent.Result.Denied(textOf("The server is full."))
      }
    }

    TerreEventBus.postAsyncWithFuture(ClientLoginEvent(this, result))
      .thenAcceptAsync({ event ->
        if (clientConnection.isClosed)
          return@thenAcceptAsync
        if (ProxyImpl.mutablePlayers.addIfAbsent(this) != null) {
          disconnectByDuplicateId()
          return@thenAcceptAsync
        }
        val eventResult = event.result
        if (eventResult is ClientLoginEvent.Result.Denied) {
          clientConnection.close(eventResult.reason)
        } else {
          TerreEventBus.postAsyncWithFuture(ClientPostLoginEvent(this))
            .thenAccept { afterLogin() }
        }
      }, clientConnection.eventLoop)
  }

  private fun afterLogin() {
    // Try to connect to one of the servers
    val possibleServers = ProxyImpl.servers.asSequence()
      .filter { it.allowAutoJoin }
      .toList()
    connectToAnyWithFuture(possibleServers).whenComplete { connected, _ ->
      if (connected == null)
        disconnectAndForget(textOf("Failed to connect to a server."))
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
   * Called when the player loses connection to the backing server.
   */
  fun disconnectedFromServer(connection: ServerConnectionImpl) {
    if (serverConnection != connection)
      return
    serverConnection = null

    val server = connection.server
    // Evacuate the player to another server
    val possibleServers = ProxyImpl.servers.asSequence()
      .filter { it.allowAutoJoin && it != server }
      .toList()
    connectToAnyWithFuture(possibleServers).whenComplete { connected, _ ->
      if (connected == null)
        disconnectAndForget(textOf("Failed to connect to a server."))
    }
  }

  override fun connectToAnyAsync(servers: Iterable<Server>) =
    connectToAnyWithFuture(servers).asDeferred()

  fun cleanup() {
    ProxyImpl.mutablePlayers.remove(this)
    serverConnection?.connection?.close()
  }

  override fun sendMessage(message: Text) {
    clientConnection.send(ChatMessagePacket(message))
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    // If the player is on a server and the sender is also a player and on the same server,
    // send as a player chat message, this will show the text message above the head of the sender.
    if (sender is PlayerImpl) {
      val serverConnection = sender.serverConnection
      if (serverConnection != null && this.serverConnection?.server == serverConnection.server) {
        val playerId = serverConnection.playerId
        if (playerId != null) {
          clientConnection.send(PlayerChatMessagePacket(playerId, message))
          return
        }
      }
    }
    super<MessageReceiverImpl>.sendMessageAs(message, sender)
  }

  override fun openPortal(
    type: PortalType, position: Vec2f, builder: PortalBuilder.() -> Unit
  ): Portal = serverConnection!!.server.openPortalFor(type, position, builder, this)

  private fun disconnectAndForget(reason: Text) {
    clientConnection.close(reason)
    serverConnection?.connection?.close()
  }

  override fun disconnectAsync(reason: Text): Job =
    clientConnection.close(reason).toDeferred()

  fun connectToWithFuture(server: Server): CompletableFuture<ServerConnectionRequestResult> {
    server as ServerImpl
    val connection = ServerConnectionImpl(server, this)
    return connection.connect().whenComplete { result, throwable ->
      if (throwable != null) {
        Terre.logger.debug(
          "Failed to establish connection to backend server: ${server.info}", throwable)
      } else if (result is ServerConnectionRequestResult.Success) {
        val old = serverConnection?.connection
        if (old != null) {
          old.setConnectionHandler(null)
          old.close()
        }
        serverConnection?.server?.removePlayer(this)
        // Replace it with the successfully established one
        serverConnection = connection
        resetClient()
        connection.server.initPlayer(this)
        Terre.logger.debug("Successfully established connection to backend server: ${server.info}")
      }
    }
  }

  override fun connectToAsync(server: Server) = connectToWithFuture(server).asDeferred()

  private fun resetClient() {
    for (player in trackedPlayers) {
      if (player.active)
        clientConnection.send(PlayerActivePacket(player.id, false))
    }
    trackedPlayers.reset()
    for (npc in trackedNpcs) {
      if (npc.active)
        clientConnection.send(NpcUpdatePacket(npc.id, NpcType(0), Vec2f.Zero, 0))
    }
    trackedNpcs.reset()
    for (item in trackedItems) {
      if (item.active)
        clientConnection.send(SimpleItemUpdatePacket(item.id, Vec2f.Zero, ItemStack.Empty))
    }
    trackedItems.reset()
    for (projectile in trackedProjectiles) {
      if (projectile.active)
        clientConnection.send(ProjectileDestroyPacket(projectile.id, projectile.owner))
    }
    trackedProjectiles.reset()
  }
}
