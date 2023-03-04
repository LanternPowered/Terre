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

import org.lanternpowered.terre.math.Vec2f

interface CombatTextAware {

  /**
   * Shows combat [text] at the given [position]. Text [color] is only supported at the root
   * element.
   */
  fun showCombatText(text: TextLike, position: Vec2f) =
    showCombatText(text.text(), position)

  /**
   * Shows combat [text] at the given [position]. Text [color] is only supported at the root
   * element.
   */
  fun showCombatText(text: Text, position: Vec2f)
}
