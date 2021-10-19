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

import org.lanternpowered.terre.impl.network.PacketDirection
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedDecoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestEncoder
import org.lanternpowered.terre.impl.network.packet.DisconnectDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordRequestDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordResponseEncoder
import org.lanternpowered.terre.impl.network.protocol

internal val ServerInitProtocol = protocol("server-init") {
  bind(0x01, ConnectionRequestEncoder, PacketDirection.ClientToServer)
  bind(0x02, DisconnectDecoder, PacketDirection.ServerToClient)
  bind(0x03, ConnectionApprovedDecoder, PacketDirection.ServerToClient)
  bind(0x25, PasswordRequestDecoder, PacketDirection.ServerToClient)
  bind(0x26, PasswordResponseEncoder, PacketDirection.ClientToServer)
}
