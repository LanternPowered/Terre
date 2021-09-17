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
import org.lanternpowered.terre.impl.network.buffer.readColor
import org.lanternpowered.terre.impl.network.buffer.readTaggedText
import org.lanternpowered.terre.impl.network.buffer.writeColor
import org.lanternpowered.terre.impl.network.buffer.writeTaggedText
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.text.ColorableText
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.color
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.Colors

internal data class ChatMessagePacket(
  val text: Text,
  val maxWidth: Int = -1
) : Packet

internal val ChatMessageEncoder = PacketEncoder<ChatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writeColor(color)
  buf.writeTaggedText(text)
  buf.writeShortLE(packet.maxWidth)
}

internal val ChatMessageDecoder = PacketDecoder { buf ->
  val color = buf.readColor()
  val text = buf.readTaggedText().color(color)
  val maxWidth = buf.readUnsignedShortLE()
  ChatMessagePacket(text, maxWidth)
}

internal object ChatMessageHelper {

  fun splitTextAndColor(text: Text, defaultColor: Color = Colors.White): Pair<Text, Color> {
    var color = defaultColor
    if (text is ColorableText) {
      color = text.color ?: defaultColor
      if (text.color != null)
        return text.color(null) to color
    }
    return text to color
  }
}
