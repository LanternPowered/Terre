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

import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.catalog.NumericCatalogTypeRegistry
import org.lanternpowered.terre.impl.item.ItemTypeRegistryImpl
import org.lanternpowered.terre.text.ItemText
import org.lanternpowered.terre.text.TextLike

/**
 * Represents the type of item.
 */
interface ItemType : NumericCatalogType, TextLike {

  override fun text(): ItemText = ItemStack(this).text()

  companion object {

    /**
     * Represents an empty item.
     */
    val None: ItemType = ItemTypeRegistryImpl.require(0)
  }
}

/**
 * A registry for all the [ItemType]s.
 */
object ItemTypeRegistry : NumericCatalogTypeRegistry<ItemType> by ItemTypeRegistryImpl
