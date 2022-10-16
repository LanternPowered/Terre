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

import org.lanternpowered.terre.impl.ProjectileType
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.ProjectileId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readProjectileId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeProjectileId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.math.Vec2f

internal data class ProjectileUpdatePacket(
  val id: ProjectileId,
  val type: ProjectileType,
  val position: Vec2f,
  val velocity: Vec2f = Vec2f.Zero,
  val knockback: Float = 0f,
  val damage: Int = 0,
  val originalDamage: Int = 0,
  val owner: PlayerId = PlayerId.None,
  val ai0: Float = 0f,
  val ai1: Float = 0f,
  val ai2: Float = 0f,
  val uniqueId: Int = -1
) : Packet

internal val ProjectileUpdateEncoder = PacketEncoder<ProjectileUpdatePacket> { buf, packet ->
  buf.writeProjectileId(packet.id)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  buf.writePlayerId(packet.owner)
  buf.writeShortLE(packet.type.value)
  var flags = 0
  var flags2 = 0
  if (packet.ai0 != 0f)
    flags += 0x01
  if (packet.ai1 != 0f)
    flags += 0x02
  if (packet.damage != 0)
    flags += 0x10
  if (packet.knockback != 0f)
    flags += 0x20
  if (packet.originalDamage != 0)
    flags += 0x40
  if (packet.uniqueId != -1)
    flags += 0x80
  if (packet.ai2 != 0f)
    flags2 += 0x01
  if (flags2 != 0)
    flags += 0x04
  buf.writeByte(flags)
  if (flags2 != 0)
    buf.writeByte(flags2)
  if (packet.ai0 != 0f)
    buf.writeFloatLE(packet.ai0)
  if (packet.ai1 != 0f)
    buf.writeFloatLE(packet.ai1)
  if (packet.damage != 0)
    buf.writeShortLE(packet.damage)
  if (packet.knockback != 0f)
    buf.writeFloatLE(packet.knockback)
  if (packet.originalDamage != 0)
    buf.writeShortLE(packet.originalDamage)
  if (packet.uniqueId != -1)
    buf.writeShortLE(packet.uniqueId)
  if (packet.ai2 != 0f)
    buf.writeFloatLE(packet.ai2)
}

internal val ProjectileUpdateDecoder = PacketDecoder { buf ->
  val projectileId = buf.readProjectileId()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  val owner = buf.readPlayerId()
  val projectileType = ProjectileType(buf.readShortLE().toInt())
  val flags = buf.readUnsignedByte().toInt()
  val flags2 = if ((flags and 0x04) != 0) buf.readUnsignedByte().toInt() else 0
  val ai0 = if ((flags and 0x01) != 0) buf.readFloatLE() else 0f
  val ai1 = if ((flags and 0x02) != 0) buf.readFloatLE() else 0f
  val damage = if ((flags and 0x10) != 0) buf.readShortLE().toInt() else 0
  val knockback = if ((flags and 0x20) != 0) buf.readFloatLE() else 0f
  val originalDamage = if ((flags and 0x40) != 0) buf.readShortLE().toInt() else 0
  val uniqueId = if ((flags and 0x80) != 0) buf.readShortLE().toInt() else -1
  val ai2 = if ((flags2 and 0x01) != 0) buf.readFloatLE() else 0f
  ProjectileUpdatePacket(projectileId, projectileType, position, velocity, knockback,
    damage, originalDamage, owner, ai0, ai1, ai2, uniqueId)
}
