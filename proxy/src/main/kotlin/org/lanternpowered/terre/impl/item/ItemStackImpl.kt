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
import org.lanternpowered.terre.util.Bytes
import kotlin.math.max

internal class ItemStackImpl(
  var typeId: Int,
  var modifierId: Int,
  quantity: Int,
  override var modData: Bytes = Bytes.Empty,
) : ItemStack {

  constructor(type: ItemType, modifier: ItemModifier, quantity: Int, modData: Bytes = Bytes.Empty):
    this(type.numericId, modifier.numericId, quantity, modData)

  override var type: ItemType
    set(value) { typeId = value.numericId }
    get() = ItemTypeImpl(typeId)

  override var modifier: ItemModifier
    set(value) { modifierId = value.numericId }
    get() = ItemModifierImpl(modifierId)

  override val isEmpty get() = quantity <= 0 || type == ItemType.None

  override var quantity: Int = if (type == ItemType.None) 0 else quantity
    set(value) {
      check(value >= 0) { "quantity cannot be negative" }
      field = max(value, 0)
    }

  override fun similarTo(other: ItemStack): Boolean =
    other.type == type && other.modifier == modifier && modData == other.modData

  override fun copy(): ItemStack {
    return ItemStackImpl(type, modifier, quantity, modData)
  }

  override fun toString(): String = "ItemStack(" +
    "type=ItemType(numericId=$typeId)," +
    "modifier=ItemModifier(numericId=$modifierId)," +
    "quantity=$quantity" +
    ")"
}
