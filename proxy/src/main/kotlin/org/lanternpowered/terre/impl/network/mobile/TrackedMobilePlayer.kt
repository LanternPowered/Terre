/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.mobile

import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.impl.network.buffer.PlayerId

/**
 * Represents a player that is being tracked.
 */
internal class TrackedMobilePlayer(val playerId: PlayerId) {

  /**
   * The current mobile id.
   */
  var mobileId: Int = -1

  /**
   * The current position of the player in the map.
   */
  var position = Vec2f.Zero
}
