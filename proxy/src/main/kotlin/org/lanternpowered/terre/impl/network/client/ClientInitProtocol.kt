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
import org.lanternpowered.terre.impl.network.protocol
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestDecoder
import org.lanternpowered.terre.impl.network.packet.init.InitDisconnectClientEncoder

internal val ClientInitProtocol = protocol("client-init") {
  bind(0x01, ConnectionRequestDecoder, PacketDirection.ClientToServer)
  bind(0x02, InitDisconnectClientEncoder, PacketDirection.ServerToClient)
}
