/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v194

import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal val PlayerInventorySlot194Encoder = PacketEncoder<PlayerInventorySlotPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.slot)
  val data = packet.data
  buf.writeBytes(data, 0, data.readableBytes())
}

internal val PlayerInventorySlot194Decoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val slot = buf.readUnsignedByte().toInt()
  val content = buf.readBytes(buf.readableBytes())
  PlayerInventorySlotPacket(playerId, slot, content)
}
