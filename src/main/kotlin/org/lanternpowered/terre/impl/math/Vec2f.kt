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
 * Constructs a new vec2f.
 */
internal fun Vec2f(x: Float, y: Float): Vec2f
    = Vec2f((x.toRawBits().toUInt().toLong() shl 32) or y.toRawBits().toUInt().toLong())

internal inline class Vec2f(private val packed: Long) {

  companion object {

    val Zero = Vec2f(0f, 0f)

    val Left = Vec2f(-1f, 0f)

    val Right = Vec2f(1f, 0f)

    val Up = Vec2f(0f, 1f)

    val Down = Vec2f(0f, -1f)
  }

  /**
   * The x value of the vector.
   */
  val x: Float
    get() = Float.fromBits((this.packed ushr 32).toInt())

  /**
   * The y value of the vector.
   */
  val y: Float
    get() = Float.fromBits((this.packed and 0xffffffffL).toInt())

  operator fun plus(that: Vec2f): Vec2f
      = Vec2f(this.x + that.x, this.y + that.y)

  operator fun plus(that: Vec2i): Vec2f
      = Vec2f(this.x + that.x, this.y + that.y)

  operator fun plus(that: Float): Vec2f
      = Vec2f(this.x + that, this.y + that)

  operator fun minus(that: Vec2f): Vec2f
      = Vec2f(this.x - that.x, this.y - that.y)

  operator fun minus(that: Vec2i): Vec2f
      = Vec2f(this.x - that.x, this.y - that.y)

  operator fun minus(that: Float): Vec2f
      = Vec2f(this.x - that, this.y - that)

  operator fun unaryMinus(): Vec2f
      = Vec2f(-this.x, -this.y)

  operator fun unaryPlus(): Vec2f
      = this

  operator fun times(that: Vec2f): Vec2f
      = Vec2f(this.x * that.x, this.y * that.y)

  operator fun times(that: Vec2i): Vec2f
      = Vec2f(this.x * that.x, this.y * that.y)

  operator fun times(that: Float): Vec2f
      = Vec2f(this.x * that, this.y * that)

  operator fun div(that: Vec2f): Vec2f
      = Vec2f(this.x / that.x, this.y / that.y)

  operator fun div(that: Vec2i): Vec2f
      = Vec2f(this.x / that.x, this.y / that.y)

  operator fun div(that: Float): Vec2f
      = Vec2f(this.x / that, this.y / that)

  operator fun rem(that: Vec2f): Vec2f
      = Vec2f(this.x % that.x, this.y % that.y)

  operator fun rem(that: Vec2i): Vec2f
      = Vec2f(this.x % that.x, this.y % that.y)

  operator fun rem(that: Float): Vec2f
      = Vec2f(this.x % that, this.y % that)

  inline operator fun component1(): Float
      = this.x

  inline operator fun component2(): Float
      = this.y

  fun toInt(): Vec2i
      = Vec2i(this.x.toInt(), this.y.toInt())

  override fun toString(): String
      = "($x, $y)"
}
