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
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.impl.network.buffer.readColor
import org.lanternpowered.terre.impl.network.buffer.readPlainText
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.color

internal data class CombatMessagePacket(
  val position: Vec2f,
  val text: Text
) : Packet

internal val CombatMessageEncoder = PacketEncoder<CombatMessagePacket> { buf, packet ->
  val (text, color) = ChatMessageHelper.splitTextAndColor(packet.text)
  buf.writeVec2f(packet.position)
  buf.writeColor(color)
  buf.writePlainText(text)
}

internal val CombatMessageDecoder = PacketDecoder { buf ->
  val position = buf.readVec2f()
  val color = buf.readColor()
  val text = buf.readPlainText().color(color)
  CombatMessagePacket(position, text)
}
