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

/**
 * Represents the type of an item.
 */
interface Item : NumericCatalogType

/**
 * A registry for all the [Item]s.
 */
object ItemRegistry : NumericCatalogTypeRegistry<Item> by ItemRegistryImpl
