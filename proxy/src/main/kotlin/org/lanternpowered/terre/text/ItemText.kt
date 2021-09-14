/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.text

import org.lanternpowered.terre.impl.text.ItemTextImpl
import org.lanternpowered.terre.item.ItemStack

/**
 * Creates a new [ItemText] from the given stack.
 */
fun textOf(itemStack: ItemStack): ItemText =
  ItemTextImpl(itemStack)

/**
 * A text component that represents an item.
 */
interface ItemText : Text {

  /**
   * The item of this text.
   */
  val itemStack: ItemStack
}
