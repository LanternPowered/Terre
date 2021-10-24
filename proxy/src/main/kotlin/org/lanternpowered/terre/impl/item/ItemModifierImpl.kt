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
import org.lanternpowered.terre.item.ItemModifier

internal class ItemModifierImpl(
  override val numericId: Int
) : ItemModifier

internal val ItemModifierRegistryImpl =
  buildNumericCatalogTypeRegistryOf<ItemModifier>(::ItemModifierImpl)
