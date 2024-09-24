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
import io.netty.buffer.Unpooled
import org.lanternpowered.terre.impl.item.ItemStackImpl
import org.lanternpowered.terre.impl.network.buffer.readImmutableBytes
import org.lanternpowered.terre.impl.network.buffer.readVarInt
import org.lanternpowered.terre.impl.network.buffer.writeBytes
import org.lanternpowered.terre.impl.network.buffer.writeVarInt
import org.lanternpowered.terre.item.ItemStack

private val EmptyNonAirModdedData = Unpooled.buffer().run {
  // https://github.com/tModLoader/tModLoader/blob/6ed6552c23f697004716fde81ba11d199929667d/patches/tModLoader/Terraria/ModLoader/IO/ItemIO.cs#L235
  writeVarInt(0) // item type modded data
  // then hooks modded data, but those are empty by default
  readImmutableBytes()
}

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
  writeModdedItemStackData(itemStack)
}

internal fun ByteBuf.writeModdedItemStackData(itemStack: ItemStack) {
  itemStack as ItemStackImpl
  if (itemStack.typeId > 0 && itemStack.quantity > 0) { // not air
    var modData = itemStack.modData
    if (modData.isEmpty())
      modData = EmptyNonAirModdedData
    writeBytes(modData)
  }
}
