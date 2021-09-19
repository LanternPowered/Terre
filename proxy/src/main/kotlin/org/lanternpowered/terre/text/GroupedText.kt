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

import org.lanternpowered.terre.util.Color

/**
 * Represents a text component that is a combination of multiple child text components.
 */
interface GroupedText : ColorableText {

  /**
   * Represents the children of this grouped text component.
   */
  val children: List<Text>

  override fun color(color: Color?): GroupedText
}
