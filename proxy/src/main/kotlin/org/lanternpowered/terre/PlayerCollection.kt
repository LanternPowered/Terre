/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre

import kotlinx.coroutines.Job
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.text.Text

/**
 * Represents a collection of players.
 */
interface PlayerCollection : Collection<Player> {

  /**
   * Attempts to get the player for the given [PlayerIdentifier].
   */
  operator fun get(identifier: PlayerIdentifier): Player?

  /**
   * Gets whether a player with the given [PlayerIdentifier] exists in this collection.
   */
  operator fun contains(identifier: PlayerIdentifier): Boolean
}

/**
 * Disconnects all the [Player]s in the [PlayerCollection].
 */
suspend fun Collection<Player>.disconnectAll(reason: Text = DefaultDisconnectReason) {
  toList().asSequence()
    .map { it.disconnectAsync(reason) }
    .forEach { it.join() }
}

/**
 * Disconnects all the [Player]s in the [PlayerCollection].
 */
fun Collection<Player>.disconnectAllAsync(reason: Text = DefaultDisconnectReason): Job {
  return launchAsync {
    disconnectAll(reason)
  }
}
