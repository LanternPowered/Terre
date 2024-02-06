/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.ProjectileId
import java.util.BitSet

internal class ProjectileDataMap {

  private val active = BitSet(InitialCapacity)
  private var owners = ByteArray(InitialCapacity)

  fun put(id: ProjectileId, owner: PlayerId) {
    check(id.value < MaximumCapacity)
    active.set(id.value)
    if (id.value >= owners.size) {
      owners = owners.copyOf(id.value + 1)
    }
    owners[id.value] = owner.value.toByte()
  }

  fun remove(id: ProjectileId) {
    check(id.value < MaximumCapacity)
    active.clear(id.value)
    owners[id.value] = 0
  }

  fun clear() {
    active.clear()
    owners.fill(0)
  }

  fun forEach(consumer: ProjectileDataConsumer) {
    active.stream().forEach { id ->
      consumer(ProjectileId(id), PlayerId(owners[id].toUByte().toInt()))
    }
  }

  companion object {

    private const val InitialCapacity = 1001

    /**
     * The maximum capacity that we will allow
     */
    private const val MaximumCapacity = 10000
  }
}

internal fun interface ProjectileDataConsumer {
  operator fun invoke(id: ProjectileId, owner: PlayerId)
}
