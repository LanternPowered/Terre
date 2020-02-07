/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.client

import org.lanternpowered.terre.impl.network.PacketDirection
import org.lanternpowered.terre.impl.network.Protocol
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestDecoder

internal val ClientInitProtocol = Protocol(0) {
  bind(0x01, ConnectionRequestDecoder, PacketDirection.ClientToServer)
}
