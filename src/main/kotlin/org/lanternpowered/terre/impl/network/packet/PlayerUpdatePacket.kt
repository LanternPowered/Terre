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

import org.lanternpowered.terre.impl.math.Vec2f
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class PlayerUpdatePacket(
    val playerId: PlayerId,
    val position: Vec2f,
    val velocity: Vec2f?,
    val selectedItem: Int,
    val flags: Int
) : Packet

internal val PlayerUpdateEncoder = packetEncoderOf<PlayerUpdatePacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  var flags = packet.flags
  buf.writeByte(flags)
  flags = flags shr 8
  // Reset has velocity flag
  flags = flags and 0x4.inv()
  val velocity = packet.velocity
  if (velocity != null)
    flags += 0x4
  buf.writeByte(flags)
  buf.writeByte(packet.selectedItem)
  buf.writeVec2f(packet.position)
  if (velocity != null)
    buf.writeVec2f(packet.velocity)
}

internal val PacketUpdateDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  var flags = buf.readUnsignedByte().toInt()
  val flags2 = buf.readUnsignedByte().toInt()
  flags += flags2 shl 8
  val selectedItem = buf.readUnsignedByte().toInt()
  val position = buf.readVec2f()
  val velocity = if ((flags2 and 0x4) != 0) buf.readVec2f() else null
  PlayerUpdatePacket(playerId, position, velocity, selectedItem, flags)
}
