/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.portal

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.impl.ServerImpl
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.ProjectileId
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdatePacket
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.portal.PortalBuilder
import org.lanternpowered.terre.portal.PortalType
import org.lanternpowered.terre.util.AABB
import java.util.UUID

internal class PortalBuilderImpl(
  val idAllocator: () -> ProjectileId,
  val server: ServerImpl,
  val type: PortalType,
  val position: Vec2f
) : PortalBuilder {

  private var onStartCollide: suspend Portal.(player: Player) -> Unit = {}
  private var onStopCollide: suspend Portal.(player: Player) -> Unit = {}

  override fun onStartCollide(block: suspend Portal.(player: Player) -> Unit) {
    onStartCollide = block
  }

  override fun onStopCollide(block: suspend Portal.(player: Player) -> Unit) {
    onStopCollide = block
  }

  fun build(): PortalImpl {
    type as PortalTypeImpl
    val id = UUID.randomUUID()
    val projectileId = idAllocator()
    return PortalImpl(id, type, projectileId, server, position, onStartCollide, onStopCollide)
  }
}

/**
 * @property projectileId The projectile id
 */
internal class PortalImpl(
  override val id: UUID,
  override val type: PortalTypeImpl,
  val projectileId: ProjectileId,
  override val server: ServerImpl,
  override val position: Vec2f,
  val onStartCollide: suspend Portal.(player: Player) -> Unit,
  val onStopCollide: suspend Portal.(player: Player) -> Unit
) : Portal {

  /**
   * The player this portal was created for, if created through [Player.openPortal].
   */
  var player: PlayerImpl? = null

  val colliding = mutableSetOf<Player>()

  override val boundingBox: AABB = AABB.centerSize(type.size).offset(position)

  override fun close() {
    server.closePortal(this)
  }

  fun createUpdatePacket(): ProjectileUpdatePacket? {
    if (type.projectileType == null)
      return null
    return ProjectileUpdatePacket(projectileId, type.projectileType, position)
  }

  fun createDestroyPacket(): ProjectileDestroyPacket? {
    if (type.projectileType == null)
      return null
    return ProjectileDestroyPacket(projectileId, PlayerId.None)
  }
}
