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

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.PlayerCollection
import org.lanternpowered.terre.util.collection.toImmutableMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class MutablePlayerCollection private constructor(
  private val map: MutableMap<UUID, Player>
) : PlayerCollection, Collection<Player> by map.values {

  override fun get(uniqueId: UUID) = map[uniqueId]
  override fun contains(uniqueId: UUID) = map.containsKey(uniqueId)

  fun add(player: Player) {
    map[player.uniqueId] = player
  }

  fun addIfAbsent(player: Player): Player? =
    map.putIfAbsent(player.uniqueId, player)

  fun remove(player: Player): Boolean =
    map.remove(player.uniqueId, player)

  fun toImmutable() = ImmutablePlayerCollection.of(map)

  companion object {

    fun concurrent(): MutablePlayerCollection =
      MutablePlayerCollection(ConcurrentHashMap())
  }
}

internal class ImmutablePlayerCollection private constructor(
  private val map: Map<UUID, Player>
) : PlayerCollection, Collection<Player> by map.values {

  override fun get(uniqueId: UUID) = map[uniqueId]
  override fun contains(uniqueId: UUID) = map.containsKey(uniqueId)

  companion object {

    fun of(map: Map<UUID, Player>): ImmutablePlayerCollection =
      ImmutablePlayerCollection(map.toImmutableMap())
  }
}
