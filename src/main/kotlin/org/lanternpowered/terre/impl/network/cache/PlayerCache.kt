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

import org.lanternpowered.terre.impl.network.buffer.PlayerId

internal class PlayerCache {

  val names = PlayerNames(Array(capacity) { null })

  companion object {

    private const val capacity = 255
  }
}

internal inline class PlayerNames(private val array: Array<String?>) {

  operator fun get(playerId: PlayerId) = this.array[playerId.value]

  operator fun set(playerId: PlayerId, name: String) {
    this.array[playerId.value] = name
  }
}
