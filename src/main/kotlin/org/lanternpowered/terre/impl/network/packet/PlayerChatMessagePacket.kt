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
import org.lanternpowered.terre.impl.network.buffer.writeColor
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeTaggedText
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.text.Text

internal data class PlayerChatMessagePacket(
    val authorId: PlayerId,
    val text: Text
) : Packet

internal val PlayerChatMessageEncoder = packetEncoderOf<PlayerChatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writePlayerId(packet.authorId)
  buf.writeTaggedText(text)
  buf.writeColor(color)
}
