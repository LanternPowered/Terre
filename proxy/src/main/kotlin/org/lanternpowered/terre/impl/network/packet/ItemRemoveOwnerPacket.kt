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
import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.impl.network.buffer.readItemId
import org.lanternpowered.terre.impl.network.buffer.writeItemId

internal data class ItemRemoveOwnerPacket(
  val id: ItemId
) : Packet {

  companion object {

    /**
     * An item id that is not used by vanilla so can be used as a ping pong between the client
     * and server. The client will always respond with a [ItemUpdateOwnerPacket] when sending
     * a [ItemRemoveOwnerPacket].
     */
    val PingPongItemId = ItemId(400)
  }
}

internal val ItemRemoveOwnerEncoder = PacketEncoder<ItemRemoveOwnerPacket> { buf, packet ->
  buf.writeItemId(packet.id)
}

internal val ItemRemoveOwnerDecoder = PacketDecoder { buf ->
  val itemId = buf.readItemId()
  ItemRemoveOwnerPacket(itemId)
}
