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
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readTaggedText
import org.lanternpowered.terre.impl.network.buffer.writePlainText
import org.lanternpowered.terre.impl.network.buffer.writeTaggedText
import org.lanternpowered.terre.text.Text

internal data class StatusPacket(
  val statusMax: Int,
  val text: Text,
  val hidePercentage: Boolean,
  val showShadows: Boolean,
  val plainText: Boolean = false,
) : Packet

internal val StatusEncoder = PacketEncoder<StatusPacket> { buf, packet ->
  buf.writeIntLE(packet.statusMax)
  if (packet.plainText) {
    buf.writePlainText(packet.text)
  } else {
    buf.writeTaggedText(packet.text)
  }
  var flags = 0
  if (packet.hidePercentage)
    flags += 0x1
  if (packet.showShadows)
    flags += 0x2
  buf.writeByte(flags)
}

internal val StatusDecoder = PacketDecoder { buf ->
  val statusMax = buf.readIntLE()
  val statusText = buf.readTaggedText()
  val flags = buf.readUnsignedByte().toInt()
  val hidePercentage = (flags and 0x1) != 0
  val showShadows = (flags and 0x2) != 0
  StatusPacket(statusMax, statusText, hidePercentage, showShadows)
}
