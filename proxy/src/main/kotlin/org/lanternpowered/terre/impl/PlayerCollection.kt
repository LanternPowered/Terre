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
import org.lanternpowered.terre.util.collection.concurrentMapOf
import org.lanternpowered.terre.util.collection.toImmutableMap

class MutablePlayerCollection private constructor(
    private val map: MutableMap<PlayerIdentifier, Player>
) : PlayerCollection, Collection<Player> by map.values {

  override fun get(identifier: PlayerIdentifier) = this.map[identifier]
  override fun contains(identifier: PlayerIdentifier) = this.map.containsKey(identifier)

  fun add(player: Player) {
    this.map[player.identifier] = player
  }

  fun addIfAbsent(player: Player): Player? {
    return this.map.putIfAbsent(player.identifier, player)
  }

  fun remove(player: Player): Boolean {
    return this.map.remove(player.identifier, player)
  }

  fun toImmutable() = ImmutablePlayerCollection.of(this.map)

  companion object {

    fun concurrentOf(): MutablePlayerCollection
        = MutablePlayerCollection(concurrentMapOf())

    fun of(): MutablePlayerCollection
        = MutablePlayerCollection(mutableMapOf())
  }
}

class ImmutablePlayerCollection private constructor(
    private val map: Map<PlayerIdentifier, Player>
) : PlayerCollection, Collection<Player> by map.values {

  override fun get(identifier: PlayerIdentifier) = this.map[identifier]
  override fun contains(identifier: PlayerIdentifier) = this.map.containsKey(identifier)

  companion object {

    fun of(map: Map<PlayerIdentifier, Player>): ImmutablePlayerCollection =
        ImmutablePlayerCollection(map.toImmutableMap())
  }
}
