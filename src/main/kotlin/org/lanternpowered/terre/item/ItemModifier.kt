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
import org.lanternpowered.terre.impl.item.ItemModifierRegistryImpl

/**
 * Represents a modifier of an item stack.
 */
interface ItemModifier : NumericCatalogType {

  companion object {

    /**
     * The default item modifier.
     */
    val Default = ItemModifierRegistry.require(0)
  }
}

/**
 * A registry for all the [ItemModifier]s.
 */
object ItemModifierRegistry : NumericCatalogTypeRegistry<ItemModifier> by ItemModifierRegistryImpl
