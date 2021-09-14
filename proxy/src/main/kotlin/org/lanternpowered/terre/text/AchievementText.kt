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

import org.lanternpowered.terre.impl.text.AchievementTextImpl

/**
 * Creates a new [AchievementText] from the given [Achievement].
 */
fun textOf(achievement: Achievement): AchievementText =
  AchievementTextImpl(achievement)

/**
 * Represents a achievement text component.
 */
interface AchievementText : Text {

  /**
   * The achievement of the text component.
   */
  val achievement: Achievement
}
