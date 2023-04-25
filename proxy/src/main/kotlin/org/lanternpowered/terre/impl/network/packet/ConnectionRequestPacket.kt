/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString

internal data class ConnectionRequestPacket(val version: String) : Packet


internal val ConnectionRequestDecoder = PacketDecoder { buf ->
  ConnectionRequestPacket(buf.readString())
}

internal val ConnectionRequestEncoder = PacketEncoder<ConnectionRequestPacket> { buf, packet ->
  buf.writeString(packet.version)
}
