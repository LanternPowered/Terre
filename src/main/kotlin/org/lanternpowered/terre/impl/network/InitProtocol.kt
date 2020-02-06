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

import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedDecoder
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedEncoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestDecoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestEncoder
import org.lanternpowered.terre.impl.network.packet.DisconnectEncoder
import org.lanternpowered.terre.impl.network.packet.PasswordRequestDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordRequestEncoder
import org.lanternpowered.terre.impl.network.packet.PasswordResponseDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordResponseEncoder
import org.lanternpowered.terre.impl.network.packet.init.DisconnectInitDecoder

internal object InitProtocol : Protocol(0) {
  init {
    bind(0x01, ConnectionRequestEncoder, ConnectionRequestDecoder)
    bind(0x02, DisconnectEncoder, DisconnectInitDecoder)
    bind(0x03, ConnectionApprovedEncoder, ConnectionApprovedDecoder)
    bind(0x25, PasswordRequestEncoder, PasswordRequestDecoder)
    bind(0x26, PasswordResponseEncoder, PasswordResponseDecoder)
  }
}
