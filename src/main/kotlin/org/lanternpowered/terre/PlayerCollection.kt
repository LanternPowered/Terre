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
}

/**
 * Disconnects all the [Player]s in the [PlayerCollection].
 */
suspend fun PlayerCollection.disconnectAll(reason: Text = DefaultDisconnectReason) {
  this.toList().asSequence()
      .map { it.disconnectAsync(reason) }
      .forEach { it.join() }
}

/**
 * Disconnects all the [Player]s in the [PlayerCollection].
 */
fun PlayerCollection.disconnectAllAsync(reason: Text = DefaultDisconnectReason): Job {
  return launchAsync {
    disconnectAll(reason)
  }
}
