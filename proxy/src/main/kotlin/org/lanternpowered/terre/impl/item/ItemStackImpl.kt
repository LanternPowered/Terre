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

import org.lanternpowered.terre.item.Item
import org.lanternpowered.terre.item.ItemModifier
import org.lanternpowered.terre.item.ItemStack
import kotlin.math.max

internal class ItemStackImpl(
    override val item: Item,
    override var modifier: ItemModifier,
    quantity: Int
) : ItemStack {

  private var theQuantity = quantity

  override val isEmpty get() = this.quantity <= 0

  override var quantity: Int
    get() = this.theQuantity
    set(value) {
      check(value >= 0) { "quantity cannot be negative" }
      this.theQuantity = max(value, 0)
    }

  override fun copy(): ItemStack {
    return ItemStackImpl(this.item, this.modifier, this.quantity)
  }
}
