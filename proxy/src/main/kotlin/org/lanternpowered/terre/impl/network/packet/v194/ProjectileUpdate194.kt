/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v194

import org.lanternpowered.terre.impl.ProjectileType
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readProjectileId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeProjectileId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdatePacket
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal val ProjectileUpdate194Encoder = PacketEncoder<ProjectileUpdatePacket> { buf, packet ->
  buf.writeProjectileId(packet.projectileId)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  buf.writeFloatLE(packet.knockback ?: 0f)
  buf.writeShortLE(packet.damage ?: 0)
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

internal val ProjectileUpdate194Decoder = PacketDecoder { buf ->
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
  ProjectileUpdatePacket(projectileId, position, velocity, knockback, damage, damage,
    owner, projectileType, ai0, ai1, uniqueId)
}
