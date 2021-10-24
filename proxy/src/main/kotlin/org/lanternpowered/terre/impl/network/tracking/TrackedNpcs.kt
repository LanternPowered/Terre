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

import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.buffer.NpcType

internal class TrackedNpcs : Iterable<TrackedNpc> {

  private val npcs = Array(capacity) { TrackedNpc(NpcId(it)) }

  operator fun get(id: NpcId) = npcs[id.value]

  override fun iterator(): Iterator<TrackedNpc> = npcs.iterator()

  fun reset() {
    npcs.forEach(TrackedNpc::reset)
  }

  companion object {

    private const val capacity = 201
  }
}

internal class TrackedNpc(
  val id: NpcId
) {
  var type = NpcType(0)
  var name: String? = null
  var life: Int = 0

  val active: Boolean
    get() = life > 0

  fun reset() {
    type = NpcType(0)
    name = null
    life = 0
  }
}
