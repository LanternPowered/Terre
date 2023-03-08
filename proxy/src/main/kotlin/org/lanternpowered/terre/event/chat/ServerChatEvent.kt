/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.chat

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text

/**
 * An event that is thrown when the server sends a chat message to the [Player].
 *
 * @property player The player that is receiving the chat message.
 * @property server The server that is sending the chat message.
 * @property message The chat message.
 * @property sender The sender the chat message is being sent as.
 * @property cancelled If the event is cancelled, this will prevent the message from being sent.
 */
data class ServerChatEvent(
  val player: Player,
  val server: Server,
  val message: Text,
  val sender: MessageSender? = null,
  var cancelled: Boolean = false,
) : Event
