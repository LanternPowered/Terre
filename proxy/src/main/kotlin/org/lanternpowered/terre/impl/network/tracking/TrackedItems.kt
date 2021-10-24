/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.tracking

import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.item.ItemType

internal class TrackedItems : Iterable<TrackedItem> {

  private val items = Array(capacity) { TrackedItem(ItemId(it)) }

  operator fun get(id: ItemId) = items[id.value]

  override fun iterator(): Iterator<TrackedItem> = items.iterator()

  fun reset() {
    items.forEach(TrackedItem::reset)
  }

  companion object {

    private const val capacity = 401
  }
}

internal class TrackedItem(
  val id: ItemId
) {
  var type = ItemType.None

  val active: Boolean
    get() = type != ItemType.None

  fun reset() {
    type = ItemType.None
  }
}
