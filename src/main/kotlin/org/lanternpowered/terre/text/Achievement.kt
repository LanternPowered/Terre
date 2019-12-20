/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.text

import org.lanternpowered.terre.catalog.CatalogType
import org.lanternpowered.terre.catalog.CatalogTypeRegistry
import org.lanternpowered.terre.impl.AchievementRegistryImpl

interface Achievement : CatalogType {

  fun text(): AchievementText
      = textOf(this)
}

object AchievementRegistry : CatalogTypeRegistry<Achievement> by AchievementRegistryImpl
