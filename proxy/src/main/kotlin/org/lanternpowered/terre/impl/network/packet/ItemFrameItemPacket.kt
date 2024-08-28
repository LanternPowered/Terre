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
import org.lanternpowered.terre.impl.network.buffer.readItemStack
import org.lanternpowered.terre.impl.network.buffer.readVec2i
import org.lanternpowered.terre.impl.network.buffer.writeItemStack
import org.lanternpowered.terre.impl.network.buffer.writeVec2i
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.math.Vec2i

internal data class ItemFrameItemPacket(
  val pos: Vec2i,
  val itemStack: ItemStack,
) : Packet

internal val ItemFrameItemDecoder = PacketDecoder { buf ->
  val pos = buf.readVec2i()
  val itemStack = buf.readItemStack()
  ItemFrameItemPacket(pos, itemStack)
}

internal val ItemFrameItemEncoder = PacketEncoder<ItemFrameItemPacket> { buf, packet ->
  buf.writeVec2i(packet.pos)
  buf.writeItemStack(packet.itemStack)
}
