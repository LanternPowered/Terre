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

import org.lanternpowered.terre.Named
import org.lanternpowered.terre.util.NamespacedId

interface CatalogType : Named {

  /**
   * The id of the catalog type.
   */
  val id: NamespacedId
}
