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

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.impl.network.buffer.readItemId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writeItemId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.item.ItemModifierRegistry
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.item.ItemTypeRegistry
import org.lanternpowered.terre.math.Vec2f

internal data class ItemUpdatePacket(
  val id: ItemId,
  val position: Vec2f,
  val stack: ItemStack,
  val velocity: Vec2f = Vec2f.Zero,
  val noDelay: Boolean = false,
) : Packet

internal val ItemUpdateEncoder = PacketEncoder<ItemUpdatePacket> { buf, packet ->
  buf.writeItemUpdate(packet.id, packet.position, packet.stack, packet.velocity,  packet.noDelay)
}

internal fun ByteBuf.writeItemUpdate(
  id: ItemId, position: Vec2f, stack: ItemStack, velocity: Vec2f, noDelay: Boolean
) {
  writeItemId(id)
  writeVec2f(position)
  writeVec2f(velocity)
  writeShortLE(stack.quantity)
  writeByte(stack.modifier.numericId)
  writeBoolean(noDelay)
  writeShortLE(stack.type.numericId)
}

internal val ItemUpdateDecoder = PacketDecoder { buf -> buf.readItemUpdate(::ItemUpdatePacket) }

internal inline fun <R> ByteBuf.readItemUpdate(
  packet: (id: ItemId, position: Vec2f, stack: ItemStack, velocity: Vec2f, noDelay: Boolean) -> R
): R {
  val id = readItemId()
  val position = readVec2f()
  val velocity = readVec2f()
  val quantity = readShortLE().toInt()
  val modifier = ItemModifierRegistry.require(readByte().toInt())
  val noDelay = readBoolean()
  val type = ItemTypeRegistry.require(readShortLE().toInt())
  val stack = ItemStack(type, modifier, quantity)
  return packet(id, position, stack, velocity, noDelay)
}
