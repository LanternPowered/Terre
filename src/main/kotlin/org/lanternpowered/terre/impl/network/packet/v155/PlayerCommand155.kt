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

import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readColor
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeColor
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.util.Colors

internal val PlayerCommand155Encoder = packetEncoderOf<PlayerCommandPacket> { buf, packet ->
  buf.writePlayerId(PlayerId(0))
  buf.writeColor(Colors.White)
  buf.writeString(if (packet.commandId == "Say") {
    // Zero space character + content
    '\u200B' + packet.arguments
  } else {
    val commandId = when (packet.commandId) {
      "Playing" -> "playing"
      "Roll" -> "roll"
      "Emote" -> "me"
      else -> packet.commandId
    }
    '/' + commandId + ' ' + packet.arguments
  })
}

internal val PlayerCommand155Decoder = packetDecoderOf { buf ->
  buf.readPlayerId()
  buf.readColor()
  var message = buf.readString()
  if (message.startsWith("/")) {
    message = message.substring(1)
    val index = message.indexOf(' ')
    var commandId = if (index == -1) message else message.substring(0, index)
    commandId = when (commandId) {
      "playing", "players" -> "Playing"
      "roll" -> "Roll"
      "me" -> "Emote"
      else -> commandId
    }
    val arguments = if (index == -1) "" else message.substring(index + 1)
    PlayerCommandPacket(commandId, arguments)
  } else {
    PlayerCommandPacket("Say", message)
  }
}
