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
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.toDeferred
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import java.net.SocketAddress

internal class PlayerImpl(
    override val remoteAddress: SocketAddress,
    val clientConnection: Connection,
    override val name: String,
    override val identifier: PlayerIdentifier
) : Player {

  @Volatile override var latency = 0

  override val serverConnection: ServerConnectionImpl?
    get() = this.theServerConnection

  private val theServerConnection: ServerConnectionImpl? = null

  override fun sendMessage(message: Text) {
    this.clientConnection.send(ChatMessagePacket(message))
  }

  override fun sendMessage(message: String) {
    sendMessage(textOf(message))
  }

  override fun disconnectAsync(reason: Text): Deferred<Unit> {
    return this.clientConnection.close(reason).toDeferred()
  }
}
