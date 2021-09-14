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

import org.lanternpowered.terre.catalog.NamedCatalogType
import org.lanternpowered.terre.catalog.NamedCatalogTypeRegistry
import org.lanternpowered.terre.impl.AchievementRegistryImpl

interface Achievement : NamedCatalogType, TextLike {

  override fun text(): AchievementText = textOf(this)
}

object AchievementRegistry : NamedCatalogTypeRegistry<Achievement> by AchievementRegistryImpl
