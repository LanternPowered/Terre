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

import org.lanternpowered.terre.impl.buildNumericCatalogTypeRegistryOf
import org.lanternpowered.terre.item.Item

internal class ItemImpl(
    override val numericId: Int
) : Item

internal val ItemRegistryImpl = buildNumericCatalogTypeRegistryOf<Item>(::ItemImpl)
