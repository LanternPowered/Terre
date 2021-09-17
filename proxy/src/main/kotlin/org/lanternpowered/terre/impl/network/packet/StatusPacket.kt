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
import org.lanternpowered.terre.impl.network.buffer.readPlainText
import org.lanternpowered.terre.impl.network.buffer.writePlainText
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.text.Text

internal data class StatusPacket(
  val statusMax: Int,
  val statusText: Text,
  val flags: Int // TODO: What flags?
) : Packet

internal val StatusEncoder = PacketEncoder<StatusPacket> { buf, packet ->
  buf.writeIntLE(packet.statusMax)
  buf.writePlainText(packet.statusText)
  buf.writeByte(packet.flags)
}

internal val StatusDecoder = PacketDecoder { buf ->
  val statusMax = buf.readIntLE()
  val statusText = buf.readPlainText()
  val flags = buf.readUnsignedByte().toInt()
  StatusPacket(statusMax, statusText, flags)
}
