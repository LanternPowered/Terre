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
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.toDeferred
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import java.net.SocketAddress

internal class PlayerImpl(
    val clientConnection: Connection,
    override val name: String,
    override val identifier: PlayerIdentifier
) : Player {

  @Volatile override var latency = 0

  override val serverConnection: ServerConnectionImpl?
    get() = this.theServerConnection

  override val remoteAddress: SocketAddress
    get() = this.clientConnection.remoteAddress

  private val theServerConnection: ServerConnectionImpl? = null

  override fun sendMessage(message: Text) {
    this.clientConnection.send(ChatMessagePacket(message))
  }

  override fun sendMessage(message: String) {
    sendMessage(textOf(message))
  }

  override fun disconnectAsync(reason: Text): Job {
    return this.clientConnection.close(reason).toDeferred()
  }
}
