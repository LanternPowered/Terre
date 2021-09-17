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
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.text.Text

/**
 * Represents the pre login event. This event is thrown after all most important information
 * about the player is retrieved. This event is thrown before requesting a password from the player.
 *
 * This event is followed by a [ClientLoginEvent].
 */
data class ClientPreLoginEvent(
  val player: Player,
  var result: Result = Result.RequestPassword
) : Event {

  /**
   * Represents the result of a pre login event.
   */
  sealed class Result {

    /**
     * The player is allowed to proceed connecting to
     * the proxy.
     */
    object Allowed : Result()

    /**
     * The proxy requests the player for a password to join
     * the server.
     *
     * If the specified [password] is empty, this result will
     * act the same as [Allowed].
     *
     * @property password The password that should be used
     */
    data class RequestPassword(val password: String) : Result()

    /**
     * The player is denied to proceed connecting to the
     * proxy. The player will be disconnected with the
     * specified [reason].
     */
    data class Denied(val reason: Text) : Result()

    companion object {

      /**
       * The default [RequestPassword].
       */
      val RequestPassword: RequestPassword
        get() = RequestPassword(Proxy.password)
    }
  }
}
