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
import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.impl.network.buffer.readItemId
import org.lanternpowered.terre.impl.network.buffer.writeItemId

internal data class ItemUpdateOwnerPacket(
  val id: ItemId,
  val ownerId: PlayerId
) : Packet

internal val ItemUpdateOwnerEncoder = PacketEncoder<ItemUpdateOwnerPacket> { buf, packet ->
  buf.writeItemId(packet.id)
  buf.writePlayerId(packet.ownerId)
}

internal val ItemUpdateOwnerDecoder = PacketDecoder { buf ->
  val itemId = buf.readItemId()
  val ownerId = buf.readPlayerId()
  ItemUpdateOwnerPacket(itemId, ownerId)
}
