/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.character

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.character.CharacterStorage
import org.lanternpowered.terre.event.Event

/**
 * An event that is thrown when the character storage for a specific [Player] is initialized. A
 * character storage is initialized once when the player connects to the proxy. However, when
 * connecting to new backing servers, the inventory will be reloaded to that specific server.
 * The default implementation can be overridden by listening to this event and providing a custom
 * implementation.
 *
 * If server side characters is enabled on the backing server, that storage will be used and the
 * storage provided by this event will be ignored for that server. The storage will not be sharable
 * between servers.
 *
 * Once proxy or server side characters have been enabled for a specific player, it is no longer
 * possible to use client side characters. So if enabled, make sure that all the backing servers
 * are covered by proxy or server side characters, otherwise items will be lost.
 */
interface InitCharacterStorageEvent : Event {

  /**
   * The player whose character storage is being initialized.
   */
  val player: Player

  /**
   * Provides a custom implementation of the character storage.
   */
  fun provide(storage: CharacterStorage)
}
