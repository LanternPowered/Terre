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
import org.lanternpowered.terre.impl.network.buffer.readVarInt
import org.lanternpowered.terre.impl.network.buffer.writeVarInt
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class ClientUniqueIdPacket(
    val bytes: ByteArray
) : Packet {

  override fun equals(other: Any?)
      = other is ClientUniqueIdPacket && this.bytes contentEquals other.bytes

  override fun hashCode()
      = this.bytes.contentHashCode()
}

internal val ClientUniqueIdDecoder = packetDecoderOf { buf ->
  val length = buf.readVarInt()
  check(length == 36) { "Invalid client identifier length: $length" }
  val bytes = ByteArray(length)
  buf.readBytes(bytes)
  ClientUniqueIdPacket(bytes)
}

internal val ClientUniqueIdEncoder = packetEncoderOf<ClientUniqueIdPacket> { buf, packet ->
  buf.writeVarInt(packet.bytes.size)
  buf.writeBytes(packet.bytes)
}
