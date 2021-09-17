/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v194

import org.lanternpowered.terre.impl.network.buffer.readPlainText
import org.lanternpowered.terre.impl.network.buffer.writePlainText
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal val Status194Encoder = PacketEncoder<StatusPacket> { buf, packet ->
  buf.writeIntLE(packet.statusMax)
  buf.writePlainText(packet.statusText)
}

internal val Status194Decoder = PacketDecoder { buf ->
  val statusMax = buf.readIntLE()
  val statusText = buf.readPlainText()
  StatusPacket(statusMax, statusText, 0)
}
