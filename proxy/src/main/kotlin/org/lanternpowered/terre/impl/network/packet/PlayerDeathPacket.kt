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
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

/**
 * A packet when a player dies.
 */
internal data class PlayerDeathPacket(
  val playerId: PlayerId,
  val damage: Int,
  val hitDirection: Int,
  val pvp: Boolean,
  val reason: PlayerDamageReason
) : Packet

internal val PlayerDeathEncoder = PacketEncoder<PlayerDeathPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeDamageReason(packet.reason)
  buf.writeShortLE(packet.damage)
  buf.writeByte(packet.hitDirection)
  buf.writeBoolean(packet.pvp)
}

internal val PlayerDeathDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val reason = buf.readDamageReason()
  val damage = buf.readUnsignedShortLE()
  val hitDirection = buf.readByte().toInt()
  val pvp = buf.readBoolean()
  PlayerDeathPacket(playerId, damage, hitDirection, pvp, reason)
}
