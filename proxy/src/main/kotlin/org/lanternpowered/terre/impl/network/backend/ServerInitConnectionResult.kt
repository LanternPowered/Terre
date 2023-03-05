/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.backend

import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.text.Text

/**
 * Represents the result of a [ServerInitConnectionHandler].
 */
internal sealed class ServerInitConnectionResult {

  data class Success(val playerId: PlayerId) : ServerInitConnectionResult()

  data class Disconnected(val reason: Text?) : ServerInitConnectionResult()

  data class UnsupportedProtocol(val reason: Text?) : ServerInitConnectionResult()

  data class NotModded(val reason: Text?) : ServerInitConnectionResult()
}
