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
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class PlayerUpdatePacket(
  val playerId: PlayerId,
  val position: Vec2f,
  val velocity: Vec2f?,
  val selectedItem: Int,
  val flags: Int,
  val isSleeping: Boolean,
  val potionOfReturnData: PotionOfReturnData?
) : Packet {

  data class PotionOfReturnData(
    val originalPosition: Vec2f,
    val homePosition: Vec2f
  )
}

internal val PlayerUpdateEncoder = PacketEncoder<PlayerUpdatePacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)

  var flags = packet.flags
  // Control flags
  buf.writeByte(flags)

  // Pulley flags
  flags = flags shr 8
  // Reset has velocity flag
  flags = flags and 0x4.inv()
  val velocity = packet.velocity
  if (velocity != null)
    flags += 0x4
  buf.writeByte(flags)

  // Misc flags
  flags = flags shr 8
  val potionOfReturnData = packet.potionOfReturnData
  // Reset has potion of return data
  flags = flags and 0x40.inv()
  if (potionOfReturnData != null)
    flags += 0x40
  buf.writeByte(flags)

  buf.writeBoolean(packet.isSleeping)
  buf.writeByte(packet.selectedItem)
  buf.writeVec2f(packet.position)
  if (velocity != null)
    buf.writeVec2f(packet.velocity)
  if (potionOfReturnData != null) {
    buf.writeVec2f(potionOfReturnData.originalPosition)
    buf.writeVec2f(potionOfReturnData.homePosition)
  }
}

internal val PlayerUpdateDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  var flags = buf.readUnsignedByte().toInt()
  val flags2 = buf.readUnsignedByte().toInt()
  val flags3 = buf.readUnsignedByte().toInt()
  flags += flags2 shl 8
  flags += flags3 shl 16
  val isSleeping = buf.readBoolean()
  val selectedItem = buf.readUnsignedByte().toInt()
  val position = buf.readVec2f()
  val velocity = if ((flags2 and 0x4) != 0) buf.readVec2f() else null
  val potionOfReturnData = if ((flags3 and 0x40) != 0) {
    val originalPosition = buf.readVec2f()
    val homePosition = buf.readVec2f()
    PlayerUpdatePacket.PotionOfReturnData(originalPosition, homePosition)
  } else null
  PlayerUpdatePacket(playerId, position, velocity, selectedItem, flags, isSleeping, potionOfReturnData)
}
