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
import org.lanternpowered.terre.impl.network.buffer.readUByte
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writeItemId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.item.ItemModifierRegistry
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.item.ItemTypeRegistry
import org.lanternpowered.terre.math.Vec2f

internal sealed interface ItemUpdatePacket : Packet {
  val id: ItemId
  val position: Vec2f
  val stack: ItemStack
  val velocity: Vec2f
  val noDelay: Boolean
}

internal data class SimpleItemUpdatePacket(
  override val id: ItemId,
  override val position: Vec2f,
  override val stack: ItemStack,
  override val velocity: Vec2f = Vec2f.Zero,
  override val noDelay: Boolean = false
) : ItemUpdatePacket

internal data class InstancedItemUpdatePacket(
  override val id: ItemId,
  override val position: Vec2f,
  override val stack: ItemStack,
  override val velocity: Vec2f = Vec2f.Zero,
  override val noDelay: Boolean = false,
) : ItemUpdatePacket

internal data class ShimmeredItemUpdatePacket(
  override val id: ItemId,
  override val position: Vec2f,
  override val stack: ItemStack,
  override val velocity: Vec2f,
  override val noDelay: Boolean,
  val shimmered: Boolean,
  var shimmerTime: Float,
) : ItemUpdatePacket

internal data class CannotBeTakenByEnemiesItemUpdatePacket(
  override val id: ItemId,
  override val position: Vec2f,
  override val stack: ItemStack,
  override val velocity: Vec2f,
  override val noDelay: Boolean,
  val cannotBeTakenByEnemiesTime: Int,
) : ItemUpdatePacket

internal val ItemUpdateEncoder = PacketEncoder<ItemUpdatePacket> { buf, packet ->
  buf.writeItemId(packet.id)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  buf.writeShortLE(packet.stack.quantity)
  buf.writeByte(packet.stack.modifier.numericId)
  buf.writeBoolean(packet.noDelay)
  buf.writeShortLE(packet.stack.type.numericId)
  if (packet is ShimmeredItemUpdatePacket) {
    buf.writeBoolean(packet.shimmered)
    buf.writeFloatLE(packet.shimmerTime)
  }
  if (packet is CannotBeTakenByEnemiesItemUpdatePacket) {
    buf.writeByte(packet.cannotBeTakenByEnemiesTime)
  }
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
  CannotBeTakenByEnemiesItemUpdatePacket(id, position, stack, velocity, noDelay,
    cannotBeTakenByEnemiesTime)
}

private inline fun <P : Packet> itemUpdateDecoder(
  crossinline packet: ByteBuf.(
    id: ItemId, position: Vec2f, stack: ItemStack, velocity: Vec2f, noDelay: Boolean
  ) -> P
) = PacketDecoder { buf ->
  val id = buf.readItemId()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  val quantity = buf.readShortLE().toInt()
  val modifier = ItemModifierRegistry.require(buf.readByte().toInt())
  val noDelay = buf.readBoolean()
  val type = ItemTypeRegistry.require(buf.readShortLE().toInt())
  val stack = ItemStack(type, modifier, quantity)
  buf.packet(id, position, stack, velocity, noDelay)
}
