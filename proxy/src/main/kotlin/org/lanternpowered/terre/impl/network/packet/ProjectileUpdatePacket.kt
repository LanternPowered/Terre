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
import org.lanternpowered.terre.impl.network.buffer.ProjectileType
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readProjectileId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeProjectileId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class ProjectileUpdatePacket(
    val projectileId: ProjectileId,
    val position: Vec2f,
    val velocity: Vec2f,
    val knockback: Float,
    val damage: Int,
    val owner: PlayerId,
    val type: ProjectileType,
    val ai0: Float?,
    val ai1: Float?,
    val uniqueId: Int?
) : Packet

internal val ProjectileUpdateEncoder = packetEncoderOf<ProjectileUpdatePacket> { buf, packet ->
  buf.writeProjectileId(packet.projectileId)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  buf.writeFloatLE(packet.knockback)
  buf.writeShortLE(packet.damage)
  buf.writePlayerId(packet.owner)
  buf.writeShortLE(packet.type.value)
  var flags = 0
  if (packet.ai0 != null)
    flags += 0x01
  if (packet.ai1 != null)
    flags += 0x02
  if (packet.uniqueId != null)
    flags += 0x04
  buf.writeByte(flags)
  if (packet.ai0 != null)
    buf.writeFloatLE(packet.ai0)
  if (packet.ai1 != null)
    buf.writeFloatLE(packet.ai1)
  if (packet.uniqueId != null)
    buf.writeShortLE(packet.uniqueId)
}

internal val ProjectileUpdateDecoder = packetDecoderOf { buf ->
  val projectileId = buf.readProjectileId()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  val knockback = buf.readFloatLE()
  val damage = buf.readShortLE().toInt()
  val owner = buf.readPlayerId()
  val projectileType = ProjectileType(buf.readShortLE().toInt())
  val flags = buf.readUnsignedByte().toInt()
  val ai0 = if ((flags and 0x01) != 0) buf.readFloatLE() else null
  val ai1 = if ((flags and 0x02) != 0) buf.readFloatLE() else null
  val uniqueId = if ((flags and 0x04) != 0) buf.readShortLE().toInt() else null
  ProjectileUpdatePacket(projectileId, position, velocity, knockback, damage, owner, projectileType, ai0, ai1, uniqueId)
}
