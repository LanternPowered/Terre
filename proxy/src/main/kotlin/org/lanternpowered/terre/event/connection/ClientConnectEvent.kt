/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.connection

import org.lanternpowered.terre.InboundConnection
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.text.Text

/**
 * An event that's thrown when a client establishes a new
 * connection with the proxy.
 *
 * @property inboundConnection The inbound connection
 * @property result The result of the event
 */
data class ClientConnectEvent(
  val inboundConnection: InboundConnection,
  var result: Result = Result.Allowed
) : Event {

  /**
   * Represents the result of a [ClientConnectEvent].
   */
  sealed class Result {

    /**
     * The client is allowed to proceed connecting to the proxy.
     */
    object Allowed : Result()

    /**
     * The client is denied to proceed connecting to the
     * proxy. The client will be disconnected with the
     * specified [reason].
     */
    data class Denied(val reason: Text) : Result()
  }
}
