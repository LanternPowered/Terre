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

import org.lanternpowered.terre.impl.item.ItemStackImpl
import org.lanternpowered.terre.text.ItemText
import org.lanternpowered.terre.text.textOf

/**
 * Constructs a new [ItemStack].
 */
fun itemStackOf(item: Item, modifier: ItemModifier = ItemModifier.Default, quantity: Int = 1): ItemStack {
  return ItemStackImpl(item, modifier, quantity)
}

/**
 * Represents a stack of items.
 */
interface ItemStack {

  /**
   * Whether this stack is empty.
   */
  val isEmpty: Boolean

  /**
   * The item type of the stack.
   */
  val item: Item

  /**
   * The quantity of items in the stack.
   */
  var quantity: Int

  /**
   * The modifier of the item, also known as "prefix".
   */
  var modifier: ItemModifier

  /**
   * Creates a copy of this stack.
   */
  fun copy(): ItemStack

  /**
   * Converts this stack into a text component.
   */
  fun text(): ItemText
      = textOf(this)
}
