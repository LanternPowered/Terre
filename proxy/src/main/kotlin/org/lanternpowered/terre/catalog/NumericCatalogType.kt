/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.catalog

/**
 * Represents a catalog type that holds a numeric id.
 */
interface NumericCatalogType : CatalogType {

  /**
   * The numeric id.
   */
  val numericId: Int
}
