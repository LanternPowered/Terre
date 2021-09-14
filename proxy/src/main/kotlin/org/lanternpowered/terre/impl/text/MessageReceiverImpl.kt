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

import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.MessageReceiver
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.color
import org.lanternpowered.terre.text.text

interface MessageReceiverImpl : MessageReceiver {

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    if (sender !is PlayerImpl) {
      sendMessage(message)
      return
    }
    val color = sender.team.color
    val name = sender.name
    sendMessage(("<$name> ".text() + message).color(color))
  }
}
