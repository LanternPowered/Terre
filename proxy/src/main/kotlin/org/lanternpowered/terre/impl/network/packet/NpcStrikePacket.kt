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
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.buffer.readNpcId
import org.lanternpowered.terre.impl.network.buffer.writeNpcId

internal data class NpcStrikePacket(
  val id: NpcId,
  val damage: Int,
  val sourceDamage: Int,
  // https://github.com/tModLoader/tModLoader/blob/1.4.4/patches/tModLoader/Terraria/ModLoader/DamageClassLoader.cs
  val damageType: Int,
  val knockback: Float,
  val hitDirection: Int,
  val crit: Boolean,
  val instantKill: Boolean,
  val hideCombatText: Boolean,
) : Packet

internal val NpcStrikeDecoder = PacketDecoder { buf ->
  val npcId = buf.readNpcId()
  val damage = buf.readShortLE().toInt()
  val knockback = buf.readFloatLE()
  val hitDirection = buf.readByte().toInt() - 1
  val crit = buf.readBoolean()
  NpcStrikePacket(
    id = npcId,
    damage = damage,
    sourceDamage = damage,
    damageType = 0, // Default
    knockback = knockback,
    hitDirection = hitDirection,
    crit = crit,
    instantKill = false,
    hideCombatText = false,
  )
}

internal val NpcStrikeEncoder = PacketEncoder<NpcStrikePacket> { buf, packet ->
  buf.writeNpcId(packet.id)
  buf.writeShortLE(packet.damage)
  buf.writeFloatLE(packet.knockback)
  buf.writeByte(packet.hitDirection + 1)
  buf.writeBoolean(packet.crit)
}
