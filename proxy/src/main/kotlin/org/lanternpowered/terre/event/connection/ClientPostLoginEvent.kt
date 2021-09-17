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

/**
 * An event that's thrown when a player has logged in successfully. This event is thrown after
 * [ClientLoginEvent] if the result wasn't [ClientLoginEvent.Result.Denied].
 *
 * @property player The player that logged in
 */
data class ClientPostLoginEvent(
  val player: Player
) : Event
