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

import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.text.text

internal val Status155Encoder = packetEncoderOf<StatusPacket> { buf, packet ->
  buf.writeIntLE(packet.statusMax)
  buf.writeString(packet.statusText.toPlain())
}

internal val Status155Decoder = packetDecoderOf { buf ->
  val statusMax = buf.readIntLE()
  val statusText = buf.readString().text()
  StatusPacket(statusMax, statusText)
}
