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

import org.lanternpowered.terre.text.MessageReceiver
import org.lanternpowered.terre.text.Text

/**
 * Represents a server that the proxy knows about.
 */
interface Server : MessageReceiver {

  /**
   * The server info of this server.
   */
  val info: ServerInfo

  /**
   * All the players that are currently connected to this server.
   */
  val players: Collection<Player>

  /**
   * Broadcasts the message to all the
   * [Player]s on this server.
   */
  override fun sendMessage(message: String)

  /**
   * Broadcasts the message to all the
   * [Player]s on this server.
   */
  override fun sendMessage(message: Text)
}
