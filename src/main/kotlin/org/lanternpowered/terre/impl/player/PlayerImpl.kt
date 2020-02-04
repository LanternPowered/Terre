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
import org.lanternpowered.terre.MessageSender
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.toDeferred
import org.lanternpowered.terre.impl.text.MessageReceiverImpl
import org.lanternpowered.terre.text.Text
import java.net.SocketAddress

internal class PlayerImpl(
    val clientConnection: Connection,
    override val protocolVersion: ProtocolVersion,
    override val name: String,
    override val identifier: PlayerIdentifier
) : Player, MessageReceiverImpl {

  @Volatile override var latency = 0

  override val serverConnection: ServerConnectionImpl?
    get() = this.theServerConnection

  override val remoteAddress: SocketAddress
    get() = this.clientConnection.remoteAddress

  private var theServerConnection: ServerConnectionImpl? = null

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

  override fun disconnectAsync(reason: Text): Job {
    return this.clientConnection.close(reason).toDeferred()
  }
}
