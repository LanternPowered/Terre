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
import org.lanternpowered.terre.impl.network.buffer.writePlainText
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.impl.math.Vec2f
import org.lanternpowered.terre.text.Text

internal data class CombatMessagePacket(
    val position: Vec2f,
    val text: Text
) : Packet

internal val CombatMessageEncoder = packetEncoderOf<CombatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writeVec2f(packet.position)
  buf.writeColor(color)
  buf.writePlainText(text)
}
