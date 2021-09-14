/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NOTHING_TO_INLINE")

package org.lanternpowered.terre.impl.util

import org.lanternpowered.terre.util.Color

/**
 * Wraps the color into a [OptionalColor].
 */
internal inline fun Color.optional(): OptionalColor =
  OptionalColor.of(this)

/**
 * Wraps the nullable color into a [OptionalColor].
 */
internal inline fun Color?.optionalFromNullable(): OptionalColor =
  OptionalColor.ofNullable(this)

/**
 * Represents a color object that can be present or absent.
 */
@JvmInline
internal value class OptionalColor(
  private val packed: Int
) : Optional<Color> {

  override val isPresent: Boolean
    get() = (packed.toLong() and 0x80_00_00_00) == 0L

  override val isEmpty: Boolean
    get() = !isPresent

  override val value: Color
    get() {
      check(isPresent) { "No value is present." }
      return Color(packed and 0x00_ff_ff_ff)
    }

  override fun orNull(): Color? =
    if (isPresent) value else null

  override fun or(that: Color): Color =
    if (isPresent) value else that

  override fun toString(): String =
    if (isPresent) "Optional$value" else "OptionalColor.empty"

  companion object {

    /**
     * An empty [OptionalColor].
     */
    fun empty(): OptionalColor =
      OptionalColor(0x80_00_00_00.toInt())

    /**
     * Wraps the color into a [OptionalColor].
     */
    fun of(color: Color): OptionalColor =
      OptionalColor(color.rgb)

    /**
     * Wraps the nullable color into a [OptionalColor].
     */
    fun ofNullable(color: Color?): OptionalColor =
      if (color == null) empty() else of(color)
  }
}
