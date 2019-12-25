/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.player

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerConnection
import org.lanternpowered.terre.impl.network.Connection
import java.util.concurrent.CompletableFuture

internal class ServerConnectionImpl(
    override val server: Server,
    override val player: Player
) : ServerConnection {

  val connection: Connection?
    get() = this.theConnection

  private var theConnection: Connection? = null

  fun connect(): CompletableFuture<Boolean> {
    TODO()
  }

  fun ensureConnected(): Connection {
    return this.connection ?: error("Not connected!")
  }
}
