/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v155

import org.lanternpowered.terre.impl.network.buffer.readColor
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packetDecoderOf

internal val PlayerCommand155Decoder = packetDecoderOf { buf ->
  buf.readPlayerId()
  buf.readColor()
  var message = buf.readString()
  if (message.startsWith("/")) {
    message = message.substring(1)
    val index = message.indexOf(' ')
    val commandId = if (index == -1) message else message.substring(0, index)
    val arguments = if (index == -1) "" else message.substring(index + 1)
    PlayerCommandPacket(commandId, arguments)
  } else {
    PlayerCommandPacket("Say", message)
  }
}
