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
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.text.text

internal val Status155Encoder = PacketEncoder<StatusPacket> { buf, packet ->
  buf.writeIntLE(packet.statusMax)
  buf.writeString(packet.statusText.toPlain())
}

internal val Status155Decoder = PacketDecoder { buf ->
  val statusMax = buf.readIntLE()
  val statusText = buf.readString().text()
  StatusPacket(statusMax, statusText, 0)
}
