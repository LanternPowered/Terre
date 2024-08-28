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
import org.lanternpowered.terre.impl.network.buffer.ChestId
import org.lanternpowered.terre.impl.network.buffer.readChestId
import org.lanternpowered.terre.impl.network.buffer.readItemStack
import org.lanternpowered.terre.impl.network.buffer.writeChestId
import org.lanternpowered.terre.impl.network.buffer.writeItemStack
import org.lanternpowered.terre.item.ItemStack

internal data class ChestItemPacket(
  val chestId: ChestId,
  val slot: Int,
  val itemStack: ItemStack,
) : Packet

internal val ChestItemDecoder = PacketDecoder { buf ->
  val chestId = buf.readChestId()
  val slot = buf.readUnsignedByte().toInt()
  val itemStack = buf.readItemStack()
  ChestItemPacket(chestId, slot, itemStack)
}

internal val ChestItemEncoder = PacketEncoder<ChestItemPacket> { buf, packet ->
  buf.writeChestId(packet.chestId)
  buf.writeByte(packet.slot)
  buf.writeItemStack(packet.itemStack)
}
