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

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.item.ItemStackImpl
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.impl.network.buffer.readImmutableBytes
import org.lanternpowered.terre.impl.network.buffer.readItemId
import org.lanternpowered.terre.impl.network.buffer.readUByte
import org.lanternpowered.terre.impl.network.buffer.readVarInt
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writeBytes
import org.lanternpowered.terre.impl.network.buffer.writeItemId
import org.lanternpowered.terre.impl.network.buffer.writeVarInt
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.packet.CannotBeTakenByEnemiesItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.InstancedItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ShimmeredItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.SimpleItemUpdatePacket
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.math.Vec2f

internal val ItemUpdateEncoder = PacketEncoder<ItemUpdatePacket> { buf, packet ->
  buf.writeItemId(packet.id)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  buf.writeVarInt(packet.itemStack.quantity)
  buf.writeVarInt(packet.itemStack.modifier.numericId)
  buf.writeBoolean(packet.noDelay)
  buf.writeShortLE(packet.itemStack.type.numericId)
  if (packet is ShimmeredItemUpdatePacket) {
    buf.writeBoolean(packet.shimmered)
    buf.writeFloatLE(packet.shimmerTime)
  }
  if (packet is CannotBeTakenByEnemiesItemUpdatePacket) {
    buf.writeByte(packet.cannotBeTakenByEnemiesTime)
  }
  buf.writeModdedItemStackData(packet.itemStack)
}

internal val SimpleItemUpdateDecoder = itemUpdateDecoder {
    id, position, stack, velocity, noDelay ->
  SimpleItemUpdatePacket(id, position, stack, velocity, noDelay)
}

internal val InstancedItemUpdateDecoder = itemUpdateDecoder {
    id, position, stack, velocity, noDelay ->
  InstancedItemUpdatePacket(id, position, stack, velocity, noDelay)
}

internal val ShimmeredItemUpdateDecoder = itemUpdateDecoder {
    id, position, stack, velocity, noDelay ->
  val shimmered = readBoolean()
  val shimmerTime = readFloatLE()
  ShimmeredItemUpdatePacket(id, position, stack, velocity, noDelay, shimmered, shimmerTime)
}

internal val CannotBeTakenByEnemiesItemUpdateDecoder = itemUpdateDecoder {
    id, position, stack, velocity, noDelay ->
  val cannotBeTakenByEnemiesTime = readUByte().toInt()
  CannotBeTakenByEnemiesItemUpdatePacket(id, position, stack, velocity, noDelay, cannotBeTakenByEnemiesTime)
}

private inline fun <P : Packet> itemUpdateDecoder(
  crossinline packet: ByteBuf.(
    id: ItemId, position: Vec2f, stack: ItemStack, velocity: Vec2f, noDelay: Boolean
  ) -> P
) = PacketDecoder { buf ->
  val id = buf.readItemId()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  val quantity = buf.readVarInt()
  val modifierId = buf.readVarInt()
  val noDelay = buf.readBoolean()
  val typeId = buf.readUnsignedShortLE()
  val itemStack = ItemStackImpl(typeId, modifierId, quantity)
  val result = buf.packet(id, position, itemStack, velocity, noDelay)
  val modData = buf.readImmutableBytes()
  itemStack.modData = modData
  result
}
