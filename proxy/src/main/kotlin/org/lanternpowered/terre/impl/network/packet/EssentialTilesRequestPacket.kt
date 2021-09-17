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

import org.lanternpowered.terre.math.Vec2i
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.writeVec2i
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class EssentialTilesRequestPacket(
  val position: Vec2i
) : Packet

internal val EssentialTilesRequestEncoder = PacketEncoder<EssentialTilesRequestPacket> { buf, packet ->
  buf.writeVec2i(packet.position)
}
