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
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.writeString

internal data class RealIPPacket(
  val ip: String
) : Packet

internal val RealIPEncoder = PacketEncoder<RealIPPacket> { buf, packet ->
  buf.writeShortLE(0)
  buf.writeString(packet.ip)
}
