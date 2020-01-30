/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event

/**
 * Represents an event subscription.
 */
interface EventSubscription {

  /**
   * Unregisters the event subscription.
   */
  fun unsubscribe()
}
