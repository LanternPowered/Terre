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

import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packet.PlayerDamageReason
import org.lanternpowered.terre.impl.network.packet.PlayerDeathPacket
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal val PlayerDeath155Encoder = PacketEncoder<PlayerDeathPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.hitDirection)
  buf.writeShortLE(packet.damage)
  buf.writeBoolean(packet.pvp)
  buf.writeString(packet.reason.toDeathMessage(this))
}

internal val PlayerDeath155Decoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val hitDirection = buf.readByte().toInt()
  val damage = buf.readUnsignedShortLE()
  val pvp = buf.readBoolean()
  val reason = buf.readString()
  PlayerDeathPacket(playerId, damage, hitDirection, pvp, PlayerDamageReason(custom = reason))
}
