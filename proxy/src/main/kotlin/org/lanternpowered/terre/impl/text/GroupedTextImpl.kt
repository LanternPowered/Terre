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
import org.lanternpowered.terre.text.GroupedText
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.ToStringHelper
import org.lanternpowered.terre.util.collection.contentEquals
import java.util.*

internal class GroupedTextImpl(
  override val children: List<Text>,
  override val optionalColor: OptionalColor = OptionalColor.empty()
) : ColorableTextImpl(), GroupedText {

  override fun toPlain() = StringBuilder()
    .apply { children.forEach { append(it.toPlain()) } }.toString()

  override val isEmpty get() = children.isEmpty()

  override fun color(color: Color?): GroupedTextImpl {
    val optionalColor = color.optionalFromNullable()
    return when (this.optionalColor) {
      optionalColor -> this
      else -> GroupedTextImpl(children, optionalColor)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (other !is GroupedTextImpl)
      return false
    return children contentEquals other.children && optionalColor == other.optionalColor
  }

  override fun hashCode(): Int {
    return Objects.hash(children, optionalColor)
  }

  override fun toString() = ToStringHelper(GroupedText::class).omitNullValues()
    .add("children", children)
    .add("color", color)
    .toString()
}
