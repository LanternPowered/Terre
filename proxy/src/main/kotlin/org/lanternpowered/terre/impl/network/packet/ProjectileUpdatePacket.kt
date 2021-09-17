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
import org.lanternpowered.terre.impl.network.buffer.ProjectileId
import org.lanternpowered.terre.impl.ProjectileType
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readProjectileId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeProjectileId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class ProjectileUpdatePacket(
  val projectileId: ProjectileId,
  val position: Vec2f,
  val velocity: Vec2f,
  val knockback: Float?,
  val damage: Int?,
  val originalDamage: Int?,
  val owner: PlayerId,
  val type: ProjectileType,
  val ai0: Float?,
  val ai1: Float?,
  val uniqueId: Int?
) : Packet

internal val ProjectileUpdateEncoder = PacketEncoder<ProjectileUpdatePacket> { buf, packet ->
  buf.writeProjectileId(packet.projectileId)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  buf.writePlayerId(packet.owner)
  buf.writeShortLE(packet.type.value)
  var flags = 0
  if (packet.ai0 != null)
    flags += 0x01
  if (packet.ai1 != null)
    flags += 0x02
  if (packet.damage != null)
    flags += 0x10
  if (packet.knockback != null)
    flags += 0x20
  if (packet.originalDamage != null)
    flags += 0x40
  if (packet.uniqueId != null)
    flags += 0x80
  buf.writeByte(flags)
  if (packet.ai0 != null)
    buf.writeFloatLE(packet.ai0)
  if (packet.ai1 != null)
    buf.writeFloatLE(packet.ai1)
  if (packet.damage != null)
    buf.writeShortLE(packet.damage)
  if (packet.knockback != null)
    buf.writeFloatLE(packet.knockback)
  if (packet.originalDamage != null)
    buf.writeShortLE(packet.originalDamage)
  if (packet.uniqueId != null)
    buf.writeShortLE(packet.uniqueId)
}

internal val ProjectileUpdateDecoder = PacketDecoder { buf ->
  val projectileId = buf.readProjectileId()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  val owner = buf.readPlayerId()
  val projectileType = ProjectileType(buf.readShortLE().toInt())
  val flags = buf.readUnsignedByte().toInt()
  val ai0 = if ((flags and 0x01) != 0) buf.readFloatLE() else null
  val ai1 = if ((flags and 0x02) != 0) buf.readFloatLE() else null
  val damage = if ((flags and 0x10) != 0) buf.readShortLE().toInt() else null
  val knockback = if ((flags and 0x20) != 0) buf.readFloatLE() else null
  val originalDamage = if ((flags and 0x40) != 0) buf.readShortLE().toInt() else null
  val uniqueId = if ((flags and 0x80) != 0) buf.readShortLE().toInt() else null
  ProjectileUpdatePacket(projectileId, position, velocity, knockback, damage,
    originalDamage, owner, projectileType, ai0, ai1, uniqueId)
}
