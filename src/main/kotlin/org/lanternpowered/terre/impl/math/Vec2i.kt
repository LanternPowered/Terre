/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package org.lanternpowered.terre.impl.math

/**
 * Constructs a new vec2i.
 */
internal fun Vec2i(x: Int, y: Int): Vec2i
    = Vec2i((x.toUInt().toLong() shl 32) or y.toUInt().toLong())

internal inline class Vec2i(private val packed: Long) {

  companion object {

    val Zero = Vec2i(0, 0)

    val Left = Vec2i(-1, 0)

    val Right = Vec2i(1, 0)

    val Up = Vec2i(0, 1)

    val Down = Vec2i(0, -1)
  }

  /**
   * The x value of the vector.
   */
  val x: Int
    get() = (this.packed ushr 32).toInt()

  /**
   * The y value of the vector.
   */
  val y: Int
    get() = (this.packed and 0xffffffffL).toInt()

  operator fun plus(that: Vec2i): Vec2i
      = Vec2i(this.x + that.x, this.y + that.y)

  operator fun plus(that: Vec2f): Vec2f
      = Vec2f(this.x + that.x, this.y + that.y)

  operator fun plus(that: Int): Vec2i
      = Vec2i(this.x + that, this.y + that)

  operator fun minus(that: Vec2i): Vec2i
      = Vec2i(this.x - that.x, this.y - that.y)

  operator fun minus(that: Vec2f): Vec2f
      = Vec2f(this.x - that.x, this.y - that.y)

  operator fun minus(that: Int): Vec2i
      = Vec2i(this.x - that, this.y - that)

  operator fun unaryMinus(): Vec2i
      = Vec2i(-this.x, -this.y)

  operator fun unaryPlus(): Vec2i
      = this

  operator fun times(that: Vec2i): Vec2i
      = Vec2i(this.x * that.x, this.y * that.y)

  operator fun times(that: Vec2f): Vec2f
      = Vec2f(this.x * that.x, this.y * that.y)

  operator fun times(that: Int): Vec2i
      = Vec2i(this.x * that, this.y * that)

  operator fun div(that: Vec2i): Vec2i
      = Vec2i(this.x / that.x, this.y / that.y)

  operator fun div(that: Vec2f): Vec2f
      = Vec2f(this.x / that.x, this.y / that.y)

  operator fun div(that: Int): Vec2i
      = Vec2i(this.x / that, this.y / that)

  operator fun rem(that: Vec2i): Vec2i
      = Vec2i(this.x % that.x, this.y % that.y)

  operator fun rem(that: Vec2f): Vec2f
      = Vec2f(this.x % that.x, this.y % that.y)

  operator fun rem(that: Int): Vec2i
      = Vec2i(this.x % that, this.y % that)

  inline operator fun component1(): Int
      = this.x

  inline operator fun component2(): Int
      = this.y

  fun toFloat(): Vec2f
      = Vec2f(this.x.toFloat(), this.y.toFloat())

  override fun toString(): String
      = "($x, $y)"
}
