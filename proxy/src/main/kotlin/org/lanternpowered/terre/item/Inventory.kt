/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.item

interface Inventory : Iterable<IndexedValue<ItemStack>> {

  /**
   * Gets the [ItemStack] at the specified [index].
   */
  operator fun get(index: Int): ItemStack

  /**
   * Sets the [ItemStack] at the specified [index].
   */
  operator fun set(index: Int, itemStack: ItemStack)
}
