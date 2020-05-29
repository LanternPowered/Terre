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
 * A component backed by literal text.
 */
interface LiteralText : ColorableText {

  /**
   * The literal text.
   */
  val literal: String

  override fun color(color: Color?): LiteralText
}
