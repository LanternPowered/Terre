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

import org.lanternpowered.terre.text.Text

/**
 * Represents a connection attempt to a backing server.
 *
 * @property server The server that the request is targeting
 */
sealed interface ServerConnectionRequestResult {

  val server: Server

  /**
   * When connecting to the server was successful.
   *
   * @property server The server that the request is targeting
   */
  data class Success(
    override val server: Server
  ) : ServerConnectionRequestResult

  /**
   * When a client is already connected to the target server.
   *
   * @property server The server that the request is targeting
   */
  data class AlreadyConnected(
    override val server: Server
  ) : ServerConnectionRequestResult

  /**
   * When a client is already being connected to the target or another server.
   *
   * @property server The server that the request is targeting
   */
  data class ConnectionInProgress(
    override val server: Server
  ) : ServerConnectionRequestResult

  /**
   * When the server disconnected the connection when attempting to connect.
   *
   * @property server The server that the request is targeting
   * @property reason The reason of disconnecting, if present
   */
  data class Disconnected(
    override val server: Server,
    val reason: Text? = null
  ) : ServerConnectionRequestResult
}
