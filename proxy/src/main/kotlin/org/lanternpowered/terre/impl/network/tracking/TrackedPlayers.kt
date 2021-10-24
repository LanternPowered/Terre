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

internal class TrackedPlayers : Iterable<TrackedPlayer> {

  private val players = Array(capacity) { TrackedPlayer(PlayerId(it)) }

  operator fun get(id: PlayerId) = players[id.value]

  override fun iterator(): Iterator<TrackedPlayer> = players.iterator()

  fun reset() {
    players.forEach(TrackedPlayer::reset)
  }

  companion object {

    private const val capacity = 255
  }
}

internal class TrackedPlayer(
  val id: PlayerId
) {
  var name: String? = null
  var active = false

  fun reset() {
    name = null
    active = false
  }
}
