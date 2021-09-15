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
import org.lanternpowered.terre.util.AABB
import java.util.UUID

/**
 * The builder for [Portal]s.
 */
interface PortalBuilder {

  /**
   * When a player starts colliding with the portal, this function will be called.
   */
  fun onStartCollide(block: suspend Portal.(player: Player) -> Unit)

  /**
   * When a player stop colliding with the portal, this function will be called. [onStartCollide]
   * will always be called before this.
   */
  fun onStopCollide(block: suspend Portal.(player: Player) -> Unit)
}

/**
 * Represents a portal. A portal can be spawned into a [Server] or to a [Player].
 *
 * Portals will be cleaned up automatically if a server is unregistered. They do not persist.
 */
interface Portal {

  /**
   * The id of the portal.
   */
  val id: UUID

  /**
   * The type of the portal.
   */
  val type: PortalType

  /**
   * The bounding box of the portal.
   */
  val boundingBox: AABB

  /**
   * The server this portal is located in.
   */
  val server: Server

  /**
   * The position of the portal.
   */
  val position: Vec2f

  /**
   * Closes the portal.
   */
  fun close()
}
