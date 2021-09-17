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
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.math.Vec2f

internal data class PlayerTeleportThroughPortalPacket(
  val playerId: PlayerId,
  val colorIndex: Int,
  val position: Vec2f,
  val velocity: Vec2f
) : Packet

internal val PlayerTeleportThroughPortalEncoder = PacketEncoder<PlayerTeleportThroughPortalPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortLE(packet.colorIndex)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
}

internal val PlayerTeleportThroughPortalDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val colorIndex = buf.readShortLE().toInt()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  PlayerTeleportThroughPortalPacket(playerId, colorIndex, position, velocity)
}
