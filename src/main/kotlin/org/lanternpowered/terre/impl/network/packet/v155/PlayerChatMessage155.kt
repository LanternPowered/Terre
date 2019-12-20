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

import org.lanternpowered.terre.impl.network.buffer.writeColor
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packet.ChatMessageHelper
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.impl.text.TextImpl
import org.lanternpowered.terre.impl.text.toTaggedVanillaText

internal val PlayerChatMessage155Encoder = packetEncoderOf<PlayerChatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writePlayerId(packet.authorId)
  buf.writeColor(color)
  buf.writeString((text as TextImpl).toTaggedVanillaText().toPlain())
}
