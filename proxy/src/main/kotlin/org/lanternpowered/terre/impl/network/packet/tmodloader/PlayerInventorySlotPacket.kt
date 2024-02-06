/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.tmodloader

import org.lanternpowered.terre.impl.item.ItemStackImpl
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readImmutableBytes
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readVarInt
import org.lanternpowered.terre.impl.network.buffer.writeBytes
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeVarInt
import org.lanternpowered.terre.item.ItemStack

internal data class PlayerInventorySlotPacket(
  val playerId: PlayerId,
  val slot: Int,
  val itemStack: ItemStack
) : Packet

internal val PlayerInventorySlotEncoder = PacketEncoder<PlayerInventorySlotPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortLE(packet.slot)
  val itemStack = packet.itemStack as ItemStackImpl
  buf.writeVarInt(itemStack.typeId)
  buf.writeVarInt(itemStack.modifierId)
  buf.writeVarInt(itemStack.quantity)
  buf.writeBytes(itemStack.modData)
}

internal val PlayerInventorySlotDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val slot = buf.readUnsignedShortLE()
  val typeId = buf.readVarInt()
  val modifierId = buf.readVarInt()
  val quantity = buf.readVarInt()
  val modData = buf.readImmutableBytes()
  val itemStack = ItemStackImpl(typeId, modifierId, quantity)
  itemStack.modData = modData
  PlayerInventorySlotPacket(playerId, slot, itemStack)
}
