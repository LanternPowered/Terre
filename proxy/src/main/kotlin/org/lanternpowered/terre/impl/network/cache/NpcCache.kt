/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.cache

import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.buffer.NpcType

internal class NpcCache {

  /**
   * The names of the NPCs.
   */
  private val npcs = Array(capacity) { Npc() }

  operator fun get(npcId: NpcId) = this.npcs[npcId.value]

  companion object {

    private const val capacity = 201
  }
}

internal class Npc {
  var type = NpcType(0)
  var name: String? = null
  var active = false
}
