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
import java.util.*

data class ClientUniqueIdPacket(
    val uniqueId: UUID
) : Packet

val ClientUniqueIdDecoder = packetDecoderOf { buf ->
  val uniqueId = UUID.fromString(buf.readString())
  ClientUniqueIdPacket(uniqueId)
}

val ClientUniqueIdEncoder = packetEncoderOf<ClientUniqueIdPacket> { buf, packet ->
  buf.writeString(packet.uniqueId.toString())
}
