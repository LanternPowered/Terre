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

package org.lanternpowered.terre.math

/**
 * An integer 2d vector.
 */
@JvmInline
value class Vec2i internal constructor(internal val packed: Long) {

  /**
   * Constructs a new integer 2d vector with the given x and y values.
   */
  constructor(x: Int, y: Int) :
    this((y.toUInt().toLong() shl 32) or x.toUInt().toLong())

  /**
   * The x value of the vector.
   */
  val x: Int
    get() = (packed and 0xffffffffL).toInt()

  /**
   * The y value of the vector.
   */
  val y: Int
    get() = (packed ushr 32).toInt()

  operator fun plus(that: Vec2i): Vec2i =
    Vec2i(x + that.x, y + that.y)

  operator fun plus(that: Vec2f): Vec2f =
    Vec2f(x + that.x, y + that.y)

  operator fun plus(that: Int): Vec2i =
    Vec2i(x + that, y + that)

  operator fun minus(that: Vec2i): Vec2i =
    Vec2i(x - that.x, y - that.y)

  operator fun minus(that: Vec2f): Vec2f =
    Vec2f(x - that.x, y - that.y)

  operator fun minus(that: Int): Vec2i =
    Vec2i(x - that, y - that)

  operator fun unaryMinus(): Vec2i =
    Vec2i(-x, -y)

  operator fun unaryPlus(): Vec2i = this

  operator fun times(that: Vec2i): Vec2i =
    Vec2i(x * that.x, y * that.y)

  operator fun times(that: Vec2f): Vec2f =
    Vec2f(x * that.x, y * that.y)

  operator fun times(that: Int): Vec2i =
    Vec2i(x * that, y * that)

  operator fun div(that: Vec2i): Vec2i =
    Vec2i(x / that.x, y / that.y)

  operator fun div(that: Vec2f): Vec2f =
    Vec2f(x / that.x, y / that.y)

  operator fun div(that: Int): Vec2i =
    Vec2i(x / that, y / that)

  operator fun rem(that: Vec2i): Vec2i =
    Vec2i(x % that.x, y % that.y)

  operator fun rem(that: Vec2f): Vec2f =
    Vec2f(x % that.x, y % that.y)

  operator fun rem(that: Int): Vec2i =
    Vec2i(x % that, y % that)

  inline operator fun component1(): Int = x
  inline operator fun component2(): Int = y

  fun toFloat(): Vec2f = Vec2f(x.toFloat(), y.toFloat())

  override fun toString(): String = "($x, $y)"

  companion object {

    val Zero = Vec2i(0, 0)

    val Left = Vec2i(-1, 0)

    val Right = Vec2i(1, 0)

    val Up = Vec2i(0, 1)

    val Down = Vec2i(0, -1)

    // Java specific API

    @JvmStatic
    @JvmName("x")
    internal fun x(vec: Vec2i) = vec.x
    @JvmStatic
    @JvmName("y")
    internal fun y(vec: Vec2i) = vec.y
    @JvmStatic
    @JvmName("of")
    internal fun of(x: Int, y: Int) = Vec2i(x, y)
  }
}
