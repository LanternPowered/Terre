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

import org.lanternpowered.terre.item.ItemModifier
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.item.ItemType
import kotlin.math.max

internal class ItemStackImpl(
  override val type: ItemType,
  override var modifier: ItemModifier,
  quantity: Int
) : ItemStack {

  override val isEmpty get() = quantity <= 0 || type == ItemType.None

  override var quantity: Int = if (type == ItemType.None) 0 else quantity
    set(value) {
      check(value >= 0) { "quantity cannot be negative" }
      field = max(value, 0)
    }

  override fun similarTo(other: ItemStack): Boolean =
    other.type == type && other.modifier == modifier

  override fun copy(): ItemStack {
    return ItemStackImpl(type, modifier, quantity)
  }

  override fun toString(): String = "ItemStack(type=$type,modifier=$modifier,quantity=$quantity)"
}
