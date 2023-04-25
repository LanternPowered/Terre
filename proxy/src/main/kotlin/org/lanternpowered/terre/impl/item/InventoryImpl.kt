/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.item

import org.lanternpowered.terre.item.Inventory
import org.lanternpowered.terre.item.ItemStack
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

internal class InventoryImpl : Inventory {

  private val items = ConcurrentHashMap<Int, ItemStack>()

  var maxSize: Int = 0

  override fun clear() {
    items.clear()
  }

  override fun get(index: Int): ItemStack = items.getOrElse(index) { ItemStack.Empty }

  override fun set(index: Int, itemStack: ItemStack) {
    if (itemStack.isEmpty) {
      items.remove(index)
    } else {
      items[index] = itemStack
      maxSize = max(maxSize, index + 1)
    }
  }

  override fun iterator(): Iterator<IndexedValue<ItemStack>> =
    items.asSequence().map { (index, value) -> IndexedValue(index, value) }.iterator()
}
