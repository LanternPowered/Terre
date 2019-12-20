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

import org.lanternpowered.terre.impl.InternalCatalogType
import org.lanternpowered.terre.item.Item
import org.lanternpowered.terre.util.NamespacedId

internal class ItemImpl(
    override val id: NamespacedId,
    override val name: String,
    override val internalId: Int
) : Item, InternalCatalogType

