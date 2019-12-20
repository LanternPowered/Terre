/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import org.lanternpowered.terre.catalog.CatalogType
import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.catalog.NumericId

internal interface InternalCatalogType : CatalogType, NumericCatalogType, InternalIdHolder {

  override val numericId: NumericId
    get() = NumericId(this.internalId)
}
