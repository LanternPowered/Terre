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

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.text.Text

/**
 * An event that's thrown when a player has been authenticated, but before they connect to a
 * server through a proxy.
 *
 * A [ClientLoginEvent] can have a result [Result.Denied] if the player failed to provide a valid
 * password as result of [ClientPreLoginEvent.Result.RequestPassword].
 *
 * @property player The player that is attempting to log in
 * @property result The result of the login
 */
data class ClientLoginEvent(
  val player: Player,
  var result: Result = Result.Allowed
) : Event {

  /**
   * Represents the result of a [ClientLoginEvent].
   */
  sealed class Result {

    /**
     * The player is allowed to proceed connecting to
     * the proxy.
     */
    object Allowed : Result()

    /**
     * The player is denied to proceed connecting to the
     * proxy. The player will be disconnected with the
     * specified [reason].
     */
    data class Denied(val reason: Text) : Result()
  }
}
