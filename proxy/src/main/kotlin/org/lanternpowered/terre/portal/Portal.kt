/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.portal

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.util.ColorHue
import java.util.UUID

/**
 * Represents a portal. A portal can be spawned into a [Server]
 * or to a [Player].
 *
 * Portals will be cleaned up automatically if a server
 * is unregistered. They do not persist.
 */
interface Portal {

  /**
   * The id of the portal.
   */
  val id: UUID

  /**
   * The color hue that's used for the portal.
   */
  val colorHue: ColorHue

  /**
   * The server this portal is located in.
   */
  val server: Server

  /**
   * The position of the portal.
   */
  val position: Vec2f

  /**
   * When a player walks into the portal, the [onUse] block
   * will be called. By default, nothing will happen when a
   * player attempts to use the portal.
   */
  fun onUse(onUse: suspend Portal.(player: Player) -> Unit)

  /**
   * Closes the portal.
   */
  fun close()
}
