/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.text

import org.lanternpowered.terre.MessageSender
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.ColorableText
import org.lanternpowered.terre.text.MessageReceiver
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.color
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.util.Colors

interface MessageReceiverImpl : MessageReceiver {

  override fun sendMessage(message: String) {
    sendMessage(textOf(message))
  }

  override fun sendMessageAs(message: String, sender: MessageSender) {
    sendMessageAs(textOf(message), sender)
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    if (sender !is PlayerImpl) {
      sendMessage(message)
      return
    }
    // TODO: Team color is normally used
    val color = if (message is ColorableText) message.color else Colors.White
    val name = sender.name
    sendMessage(("<$name> ".text() + message).color(color))
  }
}
