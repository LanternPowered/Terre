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
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdDecoder
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdEncoder
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedDecoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestEncoder
import org.lanternpowered.terre.impl.network.packet.DisconnectDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordRequestDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordResponseEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoEncoder
import org.lanternpowered.terre.impl.network.packet.RequestWorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.RequestWorldInfoEncoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoEncoder
import org.lanternpowered.terre.impl.network.protocol

internal val ServerInitProtocol = protocol("server-init") {
  bind(0x01, ConnectionRequestEncoder, PacketDirection.ClientToServer)
  bind(0x02, DisconnectDecoder, PacketDirection.ServerToClient)
  bind(0x03, ConnectionApprovedDecoder, PacketDirection.ServerToClient)
  bind(0x04, PlayerInfoEncoder, PlayerInfoDecoder)
  bind(0x06, RequestWorldInfoEncoder, RequestWorldInfoDecoder, PacketDirection.ClientToServer)
  bind(0x07, WorldInfoEncoder, WorldInfoDecoder, PacketDirection.ServerToClient)
  bind(0x25, PasswordRequestDecoder, PacketDirection.ServerToClient)
  bind(0x26, PasswordResponseEncoder, PacketDirection.ClientToServer)
  bind(0x44, ClientUniqueIdEncoder, ClientUniqueIdDecoder, PacketDirection.ClientToServer)
}
