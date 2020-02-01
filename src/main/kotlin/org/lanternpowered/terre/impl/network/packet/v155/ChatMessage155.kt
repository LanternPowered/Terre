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
import org.lanternpowered.terre.impl.network.packet.ChatMessageHelper
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.impl.text.TextImpl
import org.lanternpowered.terre.impl.text.fromTaggedVanillaText
import org.lanternpowered.terre.impl.text.toTaggedVanillaText
import org.lanternpowered.terre.text.color
import org.lanternpowered.terre.text.toText

internal val ChatMessage155Encoder = packetEncoderOf<ChatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writePlayerId(PlayerId.None)
  buf.writeColor(color)
  buf.writeString((text as TextImpl).toTaggedVanillaText().toPlain())
  buf.writeShortLE(packet.maxWidth)
}

internal val ChatMessage155Decoder = packetDecoderOf { buf ->
  buf.readPlayerId()
  val color = buf.readColor()
  val text = (buf.readString().toText() as TextImpl).fromTaggedVanillaText().color(color)
  val maxWidth = buf.readUnsignedShortLE()
  ChatMessagePacket(text, maxWidth)
}
