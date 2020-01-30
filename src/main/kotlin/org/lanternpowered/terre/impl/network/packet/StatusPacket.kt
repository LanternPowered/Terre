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
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.text.Text

internal data class StatusPacket(
    val statusMax: Int,
    val statusText: Text
) : Packet

internal val StatusEncoder = packetEncoderOf<StatusPacket> { buf, packet ->
  buf.writeIntLE(packet.statusMax)
  buf.writePlainText(packet.statusText)
}

internal val StatusDecoder = packetDecoderOf { buf ->
  val statusMax = buf.readIntLE()
  val statusText = buf.readPlainText()
  StatusPacket(statusMax, statusText)
}
