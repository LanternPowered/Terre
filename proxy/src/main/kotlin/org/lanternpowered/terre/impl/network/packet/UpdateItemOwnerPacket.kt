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
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class UpdateItemOwnerPacket(
  val itemId: Int,
  val playerId: PlayerId
) : Packet

internal val UpdateItemOwnerEncoder = PacketEncoder<UpdateItemOwnerPacket> { buf, packet ->
  buf.writeShortLE(packet.itemId)
  buf.writePlayerId(packet.playerId)
}

internal val UpdateItemOwnerDecoder = PacketDecoder { buf ->
  val itemId = buf.readUnsignedShortLE()
  val playerId = buf.readPlayerId()
  if (itemId == KeepAliveItemId) {
    KeepAlivePacket
  } else {
    UpdateItemOwnerPacket(itemId, playerId)
  }
}
