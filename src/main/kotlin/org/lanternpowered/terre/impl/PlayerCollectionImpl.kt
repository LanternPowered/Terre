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

class PlayerCollectionImpl(
    val map: Map<PlayerIdentifier, Player>
) : PlayerCollection, Collection<Player> by map.values {

  override fun get(identifier: PlayerIdentifier) = this.map[identifier]
}
