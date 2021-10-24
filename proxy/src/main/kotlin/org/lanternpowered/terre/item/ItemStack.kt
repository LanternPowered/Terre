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
import org.lanternpowered.terre.text.TextLike
import org.lanternpowered.terre.text.textOf

/**
 * Constructs a new [ItemStack].
 */
fun ItemStack(
  type: ItemType,
  modifier: ItemModifier = ItemModifier.Default,
  quantity: Int = 1
): ItemStack {
  return ItemStackImpl(type, modifier, quantity)
}

/**
 * Represents a stack of items.
 */
interface ItemStack : TextLike {

  /**
   * Whether this stack is empty.
   */
  val isEmpty: Boolean

  /**
   * The item type of the stack.
   */
  val type: ItemType

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
  override fun text(): ItemText = textOf(this)

  companion object {

    /**
     * Represents an empty item.
     */
    val Empty = ItemStack(ItemType.None)
  }
}
