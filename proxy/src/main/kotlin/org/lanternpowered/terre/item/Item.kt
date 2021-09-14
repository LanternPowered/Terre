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
import org.lanternpowered.terre.impl.item.ItemRegistryImpl
import org.lanternpowered.terre.text.ItemText
import org.lanternpowered.terre.text.TextLike

/**
 * Represents the type of item.
 */
interface Item : NumericCatalogType, TextLike {

  override fun text(): ItemText = itemStackOf(this).text()
}

/**
 * A registry for all the [Item]s.
 */
object ItemRegistry : NumericCatalogTypeRegistry<Item> by ItemRegistryImpl
