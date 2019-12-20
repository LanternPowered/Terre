/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import org.lanternpowered.terre.impl.network.packet.ConnectionRequestDecoder
import org.lanternpowered.terre.impl.network.packet.DisconnectEncoder

object InitProtocol : Protocol(0) {
  init {
    bind(0x01, ConnectionRequestDecoder)
    bind(0x02, DisconnectEncoder)
  }
}
