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

import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.impl.network.ClientVersion
import org.lanternpowered.terre.text.Text

internal class ServerImpl(override val info: ServerInfo) : Server {

  val mutablePlayers = MutablePlayerCollection.concurrentOf()

  /**
   * The last server version that was noticed by connecting clients. Is
   * used to speed up connection when multiple versions are possible.
   */
  @Volatile var lastKnownVersion: ClientVersion? = null

  override val players
    get() = this.mutablePlayers.toImmutable()

  override fun sendMessage(message: String) {
    this.players.forEach { it.sendMessage(message) }
  }

  override fun sendMessage(message: Text) {
    this.players.forEach { it.sendMessage(message) }
  }
}
