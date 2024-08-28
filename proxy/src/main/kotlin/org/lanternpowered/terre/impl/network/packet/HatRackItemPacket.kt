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

import org.lanternpowered.terre.impl.item.ItemStackImpl
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.item.ItemStack

internal data class HatRackItemPacket(
  val playerId: PlayerId,
  val tileEntityId: Int,
  val slot: Int,
  val itemStack: ItemStack,
) : Packet

internal val HatRackItemDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val tileEntityId = buf.readUnsignedShortLE()
  val slot = buf.readUnsignedByte().toInt()
  val typeId = buf.readUnsignedShortLE()
  val quantity = buf.readUnsignedShortLE()
  val modifierId = buf.readUnsignedByte().toInt()
  val itemStack = ItemStackImpl(typeId, modifierId, quantity)
  HatRackItemPacket(playerId, tileEntityId, slot, itemStack)
}

internal val HatRackItemEncoder = PacketEncoder<HatRackItemPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortLE(packet.tileEntityId)
  buf.writeByte(packet.slot)
  val itemStack = packet.itemStack as ItemStackImpl
  buf.writeShortLE(itemStack.typeId)
  buf.writeShortLE(itemStack.quantity)
  buf.writeByte(itemStack.modifierId)
}
