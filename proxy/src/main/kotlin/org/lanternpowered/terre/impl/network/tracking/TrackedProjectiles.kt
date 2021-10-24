/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.tracking

import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.ProjectileId

internal class TrackedProjectiles : Iterable<TrackedProjectile> {

  private val projectiles = Array(capacity) { TrackedProjectile(ProjectileId(it)) }

  operator fun get(id: ProjectileId) = projectiles[id.value]

  override fun iterator(): Iterator<TrackedProjectile> = projectiles.iterator()

  fun reset() {
    projectiles.forEach(TrackedProjectile::reset)
  }

  companion object {

    private const val capacity = 1001
  }
}

internal class TrackedProjectile(
  val id: ProjectileId
) {
  var owner = PlayerId.None
  var active = false

  fun reset() {
    owner = PlayerId.None
    active = false
  }
}
