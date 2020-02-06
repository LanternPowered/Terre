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

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.asDeferred
import org.lanternpowered.terre.MaxPlayers
import org.lanternpowered.terre.MessageSender
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerConnectionRequestResult
import org.lanternpowered.terre.event.connection.ClientLoginEvent
import org.lanternpowered.terre.event.connection.ClientPostLoginEvent
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.ServerImpl
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.client.ClientPlayConnectionHandler
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.toDeferred
import org.lanternpowered.terre.impl.text.MessageReceiverImpl
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import java.net.SocketAddress

internal class PlayerImpl(
    val clientConnection: Connection,
    override val protocolVersion: ProtocolVersion,
    override val name: String,
    override val identifier: PlayerIdentifier
) : Player, MessageReceiverImpl {

  @Volatile override var latency = 0

  override var serverConnection: ServerConnectionImpl? = null
    private set

  override val remoteAddress: SocketAddress
    get() = this.clientConnection.remoteAddress

  /**
   * Initializes the player and adds it to the proxy.
   */
  fun finishLogin(originalResult: ClientLoginEvent.Result) {
    this.clientConnection.setConnectionHandler(ClientPlayConnectionHandler(this))

    var result = originalResult
    if (result is ClientLoginEvent.Result.Allowed) {
      val maxPlayers = ProxyImpl.maxPlayers
      if (maxPlayers is MaxPlayers.Limited && ProxyImpl.players.size >= maxPlayers.amount) {
        result = ClientLoginEvent.Result.Denied(textOf("The server is full."))
      }
    }

    TerreEventBus.postAsyncWithFuture(ClientLoginEvent(this, result))
        .thenAccept { event ->
          val eventResult = event.result
          if (eventResult is ClientLoginEvent.Result.Denied) {
            this.clientConnection.close(eventResult.reason)
          } else {
            TerreEventBus.postAsyncWithFuture(ClientPostLoginEvent(this))
                .thenAccept { afterLogin() }
          }
        }
  }

  private fun afterLogin() {

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

  private fun disconnectAndForget(reason: Text) {
    this.clientConnection.close(reason)
    this.serverConnection?.connection?.close()
  }

  override fun disconnectAsync(reason: Text): Job {
    return this.clientConnection.close(reason).toDeferred()
  }

  override fun connectToAsync(server: Server): Deferred<ServerConnectionRequestResult> {
    server as ServerImpl
    val connection = ServerConnectionImpl(server, this)
    return connection.connect().asDeferred()
  }
}
