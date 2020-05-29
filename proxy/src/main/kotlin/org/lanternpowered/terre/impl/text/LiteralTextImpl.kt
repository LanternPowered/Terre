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

  override fun toPlain() = this.literal

  override val isEmpty get() = this.literal.isEmpty()

  override fun color(color: Color?): LiteralTextImpl = color(color.optionalFromNullable())

  fun color(optionalColor: OptionalColor): LiteralTextImpl =
      if (this.optionalColor == optionalColor) this else LiteralTextImpl(this.literal, optionalColor)

  override fun toString() = ToStringHelper(LiteralText::class).omitNullValues()
      .add("literal", this.literal)
      .add("color", this.color)
      .toString()
}
