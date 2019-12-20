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

import org.lanternpowered.terre.catalog.numericCatalogTypeRegistry
import org.lanternpowered.terre.item.Item
import org.lanternpowered.terre.util.Namespace

internal val ItemRegistryImpl = numericCatalogTypeRegistry<Item> {
  for (i in 1..5079) {
    register(ItemImpl(Namespace.Terre.id("item_$i"), "Item $i", i))
  }
}
