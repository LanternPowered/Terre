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
import org.lanternpowered.terre.catalog.catalogTypeRegistry
import org.lanternpowered.terre.util.NamespacedId

internal data class AchievementImpl(
    override val id: NamespacedId,
    override val name: String
) : Achievement

internal val AchievementRegistryImpl = catalogTypeRegistry<Achievement> {

}
