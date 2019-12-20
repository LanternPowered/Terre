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
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

data class ConnectionRequestPacket(val version: Int) : Packet

private const val versionPrefix = "Terraria"

val ConnectionRequestDecoder = packetDecoderOf { buf ->
  val name = buf.readString()
  ConnectionRequestPacket(name.substring(versionPrefix.length).toInt())
}

val ConnectionRequestEncoder = packetEncoderOf<ConnectionRequestPacket> { buf, packet ->
  buf.writeString(versionPrefix + packet.version)
}
