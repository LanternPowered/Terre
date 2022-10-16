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

import org.lanternpowered.terre.text.Achievement

internal data class AchievementImpl(
  override val name: String
) : Achievement

internal val AchievementRegistryImpl =
  buildNamedCatalogTypeRegistryOf<Achievement>(::AchievementImpl)
