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
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.util.collection.toImmutableMap
import java.util.concurrent.ConcurrentHashMap

internal class MutablePlayerCollection private constructor(
  private val map: MutableMap<PlayerIdentifier, Player>
) : PlayerCollection, Collection<Player> by map.values {

  override fun get(identifier: PlayerIdentifier) = map[identifier]
  override fun contains(identifier: PlayerIdentifier) = map.containsKey(identifier)

  fun add(player: Player) {
    map[player.identifier] = player
  }

  fun addIfAbsent(player: Player): Player? =
    map.putIfAbsent(player.identifier, player)

  fun remove(player: Player): Boolean =
    map.remove(player.identifier, player)

  fun toImmutable() = ImmutablePlayerCollection.of(map)

  companion object {

    fun concurrentOf(): MutablePlayerCollection =
      MutablePlayerCollection(ConcurrentHashMap())

    fun of(): MutablePlayerCollection =
      MutablePlayerCollection(HashMap())
  }
}

internal class ImmutablePlayerCollection private constructor(
  private val map: Map<PlayerIdentifier, Player>
) : PlayerCollection, Collection<Player> by map.values {

  override fun get(identifier: PlayerIdentifier) = map[identifier]
  override fun contains(identifier: PlayerIdentifier) = map.containsKey(identifier)

  companion object {

    fun of(map: Map<PlayerIdentifier, Player>): ImmutablePlayerCollection =
      ImmutablePlayerCollection(map.toImmutableMap())
  }
}
