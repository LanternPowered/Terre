/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.text

import org.lanternpowered.terre.text.Achievement
import org.lanternpowered.terre.text.AchievementText
import org.lanternpowered.terre.util.ToStringHelper

internal data class AchievementTextImpl(
    override val achievement: Achievement
) : TextImpl(), AchievementText {

  override val isEmpty: Boolean
    get() = false

  override fun toPlain(): String =
    '[' + achievement.name + ']'

  override fun toString() = ToStringHelper(AchievementText::class)
    .add("achievement", achievement)
    .toString()
}
