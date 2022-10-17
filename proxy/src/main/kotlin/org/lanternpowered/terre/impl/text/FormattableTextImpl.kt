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
import org.lanternpowered.terre.text.FormattableText
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.ToStringHelper
import org.lanternpowered.terre.util.collection.contentEquals
import java.text.MessageFormat
import java.util.*

internal class FormattableTextImpl(
  override val format: String,
  override val substitutions: List<Text>,
  override val optionalColor: OptionalColor = OptionalColor.empty()
) : ColorableTextImpl(), FormattableText {

  override fun toPlain(): String = MessageFormat.format(format,
    substitutions.stream().map { text -> text.toPlain() }.toArray())

  override val isEmpty get() = format.isEmpty()

  override fun color(color: Color?): FormattableTextImpl {
    val optionalColor = color.optionalFromNullable()
    return when (this.optionalColor) {
      optionalColor -> this
      else -> FormattableTextImpl(format, substitutions, optionalColor)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other !is FormattableTextImpl) {
      return false
    }
    return format == other.format
      && substitutions contentEquals other.substitutions
      && optionalColor == other.optionalColor
  }

  override fun hashCode(): Int =
    Objects.hash(format, substitutions, optionalColor)

  override fun toString() = ToStringHelper(FormattableText::class).omitNullValues()
    .add("format", format)
    .add("substitutions", substitutions)
    .add("color", color)
    .toString()
}
