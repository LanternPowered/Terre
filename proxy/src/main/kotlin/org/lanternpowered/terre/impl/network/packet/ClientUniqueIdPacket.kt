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

import io.netty.handler.codec.DecoderException
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import java.util.*

internal data class ClientUniqueIdPacket(
  val uniqueId: UUID
) : Packet

internal val ClientUniqueIdDecoder = PacketDecoder { buf ->
  val uniqueId = try {
    UUID.fromString(buf.readString())
  } catch (ex: IllegalArgumentException) {
    throw DecoderException(ex)
  }
  ClientUniqueIdPacket(uniqueId)
}

internal val ClientUniqueIdEncoder = PacketEncoder<ClientUniqueIdPacket> { buf, packet ->
  buf.writeString(packet.uniqueId.toString())
}
