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
 * Represents a listener registration.
 */
interface ListenerRegistration {

  /**
   * Unregisters the listener registration.
   */
  fun unregister()
}
