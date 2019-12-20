/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.writeColor
import org.lanternpowered.terre.impl.network.buffer.writeTaggedText
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.text.ColorableText
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.Colors

data class ChatMessagePacket(
    val text: Text,
    val maxWidth: Int = -1
) : Packet

val ChatMessageEncoder = packetEncoderOf<ChatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writeColor(color)
  buf.writeTaggedText(text)
  buf.writeShortLE(packet.maxWidth)
}

object ChatMessageHelper {

  fun splitTextAndColor(text: Text, defaultColor: Color = Colors.White): Pair<Text, Color> {
    var color = defaultColor
    if (text is ColorableText) {
      color = text.color ?: defaultColor
      if (text.color != null) {
        return text.color(null) to color
      }
    }
    return text to color
  }
}
