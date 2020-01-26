/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import org.lanternpowered.terre.PlayerCollection
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.text.Text

internal class ServerImpl(override val info: ServerInfo) : Server {

  override val players: PlayerCollection
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  override fun sendMessage(message: String) {
    this.players.forEach { it.sendMessage(message) }
  }

  override fun sendMessage(message: Text) {
    this.players.forEach { it.sendMessage(message) }
  }
}
