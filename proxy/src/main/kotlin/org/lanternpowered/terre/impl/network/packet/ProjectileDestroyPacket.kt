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
import org.lanternpowered.terre.impl.network.buffer.ProjectileId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readProjectileId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeProjectileId
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class ProjectileDestroyPacket(
  val id: ProjectileId,
  val owner: PlayerId
) : Packet

internal val ProjectileDestroyEncoder = PacketEncoder<ProjectileDestroyPacket> { buf, packet ->
  buf.writeProjectileId(packet.id)
  buf.writePlayerId(packet.owner)
}

internal val ProjectileDestroyDecoder = PacketDecoder { buf ->
  val projectileId = buf.readProjectileId()
  val owner = buf.readPlayerId()
  ProjectileDestroyPacket(projectileId, owner)
}
