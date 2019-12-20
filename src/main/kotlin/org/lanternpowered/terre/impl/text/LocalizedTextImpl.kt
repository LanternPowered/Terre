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
import org.lanternpowered.terre.text.LocalizedText
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.ToStringHelper
import org.lanternpowered.terre.util.collection.contentEquals
import java.util.*

internal class LocalizedTextImpl(
    override val key: String,
    override val substitutions: List<Text>,
    override val optionalColor: OptionalColor = OptionalColor.empty()
) : ColorableTextImpl(), LocalizedText {

  override fun toPlain(): String = this.key

  override val isEmpty get() = this.key.isEmpty()

  override fun color(color: Color?): LocalizedTextImpl = color(color.optionalFromNullable())

  fun color(optionalColor: OptionalColor): LocalizedTextImpl =
      if (this.optionalColor == optionalColor) this else LocalizedTextImpl(this.key, this.substitutions, optionalColor)

  override fun equals(other: Any?): Boolean {
    if (other !is LocalizedTextImpl) {
      return false
    }
    return this.key == other.key && this.substitutions contentEquals other.substitutions &&
        this.optionalColor == other.optionalColor
  }

  override fun hashCode(): Int {
    return Objects.hash(this.key, this.substitutions, this.optionalColor)
  }

  override fun toString() = ToStringHelper(LocalizedText::class).omitNullValues()
      .add("key", this.key)
      .add("substitutions", this.substitutions)
      .add("color", this.color)
      .toString()
}
