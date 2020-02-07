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
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.impl.text.TextImpl
import org.lanternpowered.terre.impl.text.fromTaggedVanillaText
import org.lanternpowered.terre.impl.text.toTaggedVanillaText
import org.lanternpowered.terre.text.color
import org.lanternpowered.terre.text.text

internal val PlayerChatMessage155Encoder = packetEncoderOf<PlayerChatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writePlayerId(packet.authorId)
  buf.writeColor(color)
  buf.writeString((text as TextImpl).toTaggedVanillaText().toPlain())
}

internal val PlayerChatMessage155Decoder = packetDecoderOf { buf ->
  var authorId = buf.readPlayerId()
  // TODO: Don't assume it's mobile
  if (authorId.value == 16) { // 16 represents none on mobile
    authorId = PlayerId.None
  }
  val color = buf.readColor()
  val raw = buf.readString().text() as TextImpl
  val text = raw.fromTaggedVanillaText().color(color)
  PlayerChatMessagePacket(authorId, text).also { print(it) }
}
