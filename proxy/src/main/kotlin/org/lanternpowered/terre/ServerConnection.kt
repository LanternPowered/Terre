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

/**
 * Represents a connection between the proxy and a server for a specific [Player].
 */
interface ServerConnection {

  /**
   * The server this connection is connected to.
   */
  val server: Server

  /**
   * The player that this connection is used by.
   */
  val player: Player
}
