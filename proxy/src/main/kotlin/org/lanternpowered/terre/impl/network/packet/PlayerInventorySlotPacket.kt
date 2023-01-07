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
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.item.ItemModifierRegistry
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.item.ItemTypeRegistry

internal data class PlayerInventorySlotPacket(
  val playerId: PlayerId,
  val slot: Int,
  val itemStack: ItemStack
) : Packet

internal val PlayerInventorySlotEncoder = PacketEncoder<PlayerInventorySlotPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortLE(packet.slot)
  val itemStack = packet.itemStack
  buf.writeShortLE(itemStack.quantity)
  buf.writeByte(itemStack.modifier.numericId)
  buf.writeShortLE(itemStack.type.numericId)
}

internal val PlayerInventorySlotDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val slot = buf.readUnsignedShortLE()
  val quantity = buf.readUnsignedShortLE()
  val modifier = ItemModifierRegistry[buf.readUnsignedByte().toInt()]!!
  val type = ItemTypeRegistry[buf.readUnsignedShortLE()]!!
  val itemStack = ItemStack(type, modifier, quantity)
  PlayerInventorySlotPacket(playerId, slot, itemStack)
}
