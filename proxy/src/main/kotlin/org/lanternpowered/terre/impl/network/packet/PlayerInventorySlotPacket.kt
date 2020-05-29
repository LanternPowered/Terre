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

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.ForwardingReferenceCounted
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class PlayerInventorySlotPacket(
    val playerId: PlayerId,
    val slot: Int,
    val data: ByteBuf
) : Packet, ForwardingReferenceCounted(data)

internal val PlayerInventorySlotEncoder = packetEncoderOf<PlayerInventorySlotPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortLE(packet.slot)
  val data = packet.data
  buf.writeBytes(data, 0, data.readableBytes())
}

internal val PlayerInventorySlotDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val slot = buf.readUnsignedShortLE()
  val content = buf.readBytes(buf.readableBytes())
  PlayerInventorySlotPacket(playerId, slot, content)
}
