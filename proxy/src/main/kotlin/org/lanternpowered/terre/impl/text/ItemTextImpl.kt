/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.text

import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.text.ItemText
import org.lanternpowered.terre.util.ToStringHelper

internal data class ItemTextImpl(
  override val itemStack: ItemStack
) : TextImpl(), ItemText {

  override val isEmpty: Boolean
    get() = this.itemStack.isEmpty

  override fun toPlain(): String =
    "[Item: Type=${itemStack.type.numericId} Modifier=${itemStack.modifier.numericId}]"

  override fun toString() = ToStringHelper(ItemText::class)
    .add("itemStack", itemStack)
    .toString()
}
