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
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readColor
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readTaggedText
import org.lanternpowered.terre.impl.network.buffer.writeColor
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeTaggedText
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.color

internal data class PlayerChatMessagePacket(
  val authorId: PlayerId,
  val text: Text
) : Packet

internal val PlayerChatMessageEncoder = PacketEncoder<PlayerChatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writePlayerId(packet.authorId)
  buf.writeTaggedText(text)
  buf.writeColor(color)
}

internal val PlayerChatMessageDecoder = PacketDecoder { buf ->
  val authorId = buf.readPlayerId()
  val text = buf.readTaggedText()
  val color = buf.readColor()
  PlayerChatMessagePacket(authorId, text.color(color))
}
