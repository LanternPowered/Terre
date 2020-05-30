/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.lanternpowered.terre.impl.network.buffer.ProjectileId

internal class ProjectileIdAllocator {

  private val allocated = IntOpenHashSet()

  fun allocate(): ProjectileId {
    for (i in 1000 downTo 0) {
      if (allocated.contains(i))
        continue
      allocated.add(i)
      return ProjectileId(i)
    }
    return ProjectileId.None
  }

  fun release(id: ProjectileId) {
    allocated.remove(id.value)
  }
}
