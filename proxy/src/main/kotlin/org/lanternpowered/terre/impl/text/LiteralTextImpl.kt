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

import org.lanternpowered.terre.impl.util.OptionalColor
import org.lanternpowered.terre.impl.util.optionalFromNullable
import org.lanternpowered.terre.text.LiteralText
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.ToStringHelper

internal data class LiteralTextImpl(
  override val literal: String,
  override val optionalColor: OptionalColor = OptionalColor.empty()
) : ColorableTextImpl(), LiteralText {

  override fun toPlain() = literal

  override val isEmpty get() = literal.isEmpty()

  override fun color(color: Color?): LiteralTextImpl =
    color(color.optionalFromNullable())

  fun color(color: OptionalColor): LiteralTextImpl =
    if (optionalColor == color) this
    else LiteralTextImpl(literal, color)

  override fun toString() = ToStringHelper(LiteralText::class)
    .omitNullValues()
    .add("literal", literal)
    .add("color", color)
    .toString()
}
