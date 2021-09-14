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
 * Represents the number of players that are allowed to join the proxy.
 */
sealed class MaxPlayers {

  /**
   * A limited number of players is allowed.
   *
   * This doesn't allow players to join if the backing servers have less space than the specified
   * [amount].
   */
  data class Limited(val amount: Int) : MaxPlayers()

  /**
   * Players are allowed to join as long that there's space on one of the backing servers.
   */
  object Unlimited : MaxPlayers()
}
