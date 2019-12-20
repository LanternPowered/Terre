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
import org.lanternpowered.terre.impl.network.packet.PlayerHurtPacket
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal val PlayerHurt155Encoder = packetEncoderOf<PlayerHurtPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.hitDirection)
  buf.writeShortLE(packet.damage)
  buf.writeString(packet.reason.toDeathMessage(this))
  var flags = 0
  if (packet.critical)
    flags += 0x1
  if (packet.pvp)
    flags += 0x2
  when (packet.cooldownCounter) {
    -1 -> flags += 0x4
    1 -> flags += 0x8
    2 -> flags += 0x10
  }
  buf.writeByte(flags)
}

internal val PlayerHurt155Decoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val hitDirection = buf.readByte().toInt()
  val damage = buf.readUnsignedShortLE()
  val reason = buf.readString()
  val flags = buf.readByte().toInt()
  val critical = (flags and 0x1) != 0
  val pvp = (flags and 0x2) != 0
  val cooldownCounter = when {
    (flags and 0x4) != 0 -> -1
    (flags and 0x8) != 0 -> 1
    (flags and 0x10) != 0 -> 2
    else -> 0
  }
  PlayerHurtPacket(playerId, damage, hitDirection, critical, pvp,
      cooldownCounter, PlayerDamageReason(custom = reason))
}
