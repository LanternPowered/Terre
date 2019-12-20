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
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.packet.ChatMessageHelper
import org.lanternpowered.terre.impl.network.packet.CombatMessagePacket
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal val CombatMessage155Encoder = packetEncoderOf<CombatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writeVec2f(packet.position)
  buf.writeColor(color)
  buf.writeString(text.toPlain())
}
