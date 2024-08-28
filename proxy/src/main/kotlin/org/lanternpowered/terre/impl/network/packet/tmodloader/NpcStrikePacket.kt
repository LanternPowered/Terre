/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.tmodloader

import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readNpcId
import org.lanternpowered.terre.impl.network.buffer.readVarInt
import org.lanternpowered.terre.impl.network.buffer.writeNpcId
import org.lanternpowered.terre.impl.network.buffer.writeVarInt
import org.lanternpowered.terre.impl.network.packet.NpcStrikePacket

// https://github.com/tModLoader/tModLoader/blob/1.4.4/patches/tModLoader/Terraria/MessageBuffer.cs.patch#L310

internal val NpcStrikeDecoder = PacketDecoder { buf ->
  val npcId = buf.readNpcId()
  val damage = buf.readVarInt()
  var sourceDamage = damage
  var damageType = 0 // Default
  var hitDirection = 0
  var knockback = 0f
  var crit = false
  var instantKill = false
  var hideCombatText = false
  if (damage >= 0) {
    sourceDamage = buf.readVarInt()
    damageType = buf.readVarInt()
    hitDirection = buf.readUnsignedByte().toInt()
    knockback = buf.readFloatLE()
    val flags = buf.readUnsignedByte().toInt()
    crit = (flags and 0x1) != 0
    instantKill = (flags and 0x2) != 0
    hideCombatText = (flags and 0x4) != 0
  }
  NpcStrikePacket(
    id = npcId,
    damage = damage,
    sourceDamage = sourceDamage,
    damageType = damageType,
    knockback = knockback,
    hitDirection = hitDirection,
    crit = crit,
    instantKill = instantKill,
    hideCombatText = hideCombatText
  )
}

internal val NpcStrikeEncoder = PacketEncoder<NpcStrikePacket> { buf, packet ->
  buf.writeNpcId(packet.id)
  buf.writeVarInt(packet.damage)
  if (packet.damage > 0) {
    buf.writeVarInt(packet.sourceDamage)
    buf.writeVarInt(packet.damageType)
    buf.writeByte(packet.hitDirection)
    buf.writeFloatLE(packet.knockback)
    var flags = 0
    if (packet.crit)
      flags += 0x1
    if (packet.instantKill)
      flags += 0x2
    if (packet.hideCombatText)
      flags += 0x4
    buf.writeByte(flags)
  }
}
