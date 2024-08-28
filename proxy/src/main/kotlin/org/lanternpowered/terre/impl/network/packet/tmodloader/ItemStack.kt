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
import org.lanternpowered.terre.impl.network.buffer.readImmutableBytes
import org.lanternpowered.terre.impl.network.buffer.readVarInt
import org.lanternpowered.terre.impl.network.buffer.writeBytes
import org.lanternpowered.terre.impl.network.buffer.writeVarInt
import org.lanternpowered.terre.item.ItemStack

internal fun ByteBuf.readModdedItemStack(): ItemStack {
  val typeId = readVarInt()
  val modifierId = readVarInt()
  val quantity = readVarInt()
  val modData = readImmutableBytes()
  return ItemStackImpl(typeId, modifierId, quantity, modData)
}

internal fun ByteBuf.writeModdedItemStack(itemStack: ItemStack) {
  itemStack as ItemStackImpl
  writeVarInt(itemStack.typeId)
  writeVarInt(itemStack.modifierId)
  writeVarInt(itemStack.quantity)
  writeBytes(itemStack.modData)
}
