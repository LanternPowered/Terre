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

import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class NebulaLevelUpRequestPacket(
  val playerId: PlayerId,
  val type: Int,
  val origin: Vec2f
) : Packet

internal val NebulaLevelUpRequestEncoder = PacketEncoder<NebulaLevelUpRequestPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.type)
  buf.writeVec2f(packet.origin)
}
