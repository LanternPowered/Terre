/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.buffer

/**
 * Represents the id of a player.
 */
@JvmInline
internal value class PlayerId(inline val value: Int) {

  /**
   * Converts this [PlayerId] to a desktop player id. For mobile servers 16 represents "none"
   * instead of 255.
   */
  fun to(isMobile: Boolean): PlayerId {
    if (isMobile && this == None)
      return MobileNone
    return this
  }

  /**
   * Converts this [PlayerId] to a desktop player id. For mobile servers 16 represents "none"
   * instead of 255.
   */
  internal fun from(isMobile: Boolean): PlayerId {
    if (isMobile && this == MobileNone)
      return None
    return this
  }

  companion object {

    /**
     * Represents no player.
     */
    val None = PlayerId(255)

    /**
     * Represents no player on mobile.
     */
    val MobileNone = PlayerId(16)
  }
}
