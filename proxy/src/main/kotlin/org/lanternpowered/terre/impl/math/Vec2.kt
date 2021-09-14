/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.math

import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.math.Vec2i
import kotlin.math.*

internal fun lengthSquared(vec2i: Vec2i): Int =
  vec2i.x * vec2i.x + vec2i.y * vec2i.y

internal fun lengthSquared(vec2f: Vec2f): Float =
  vec2f.x * vec2f.x + vec2f.y * vec2f.y

internal fun length(vec2i: Vec2i): Float =
  sqrt(lengthSquared(vec2i).toFloat())

internal fun length(vec2f: Vec2f): Float =
  sqrt(lengthSquared(vec2f))

internal fun distanceSquared(a: Vec2i, b: Vec2i): Int {
  val dx = a.x - b.x
  val dy = a.y - b.y
  return dx * dx + dy * dy
}

internal fun distanceSquared(a: Vec2f, b: Vec2f): Float {
  val dx = a.x - b.x
  val dy = a.y - b.y
  return dx * dx + dy * dy
}

internal fun distance(a: Vec2i, b: Vec2i): Float =
  sqrt(distanceSquared(a, b).toFloat())

internal fun distance(a: Vec2f, b: Vec2f): Float =
  sqrt(distanceSquared(a, b))

internal fun abs(vec: Vec2i): Vec2i =
  Vec2i(abs(vec.x), abs(vec.y))

internal fun abs(vec: Vec2f): Vec2f =
  Vec2f(abs(vec.x), abs(vec.y))

internal fun dot(a: Vec2i, b: Vec2i): Int =
  a.x * b.x + a.y * b.y

internal fun dot(a: Vec2f, b: Vec2f): Float =
  a.x * b.x + a.y * b.y

internal fun pow(vec: Vec2i, n: Int): Vec2i =
  Vec2i(vec.x.toDouble().pow(n).toInt(), vec.y.toDouble().pow(n).toInt())

internal fun pow(vec: Vec2i, x: Float): Vec2f =
  Vec2f(vec.x.toDouble().pow(x.toDouble()).toFloat(), vec.y.toDouble().pow(x.toDouble()).toFloat())

internal fun pow(vec: Vec2f, n: Int): Vec2f =
  Vec2f(vec.x.toDouble().pow(n).toFloat(), vec.y.toDouble().pow(n).toFloat())

internal fun pow(vec: Vec2f, x: Float): Vec2f =
  Vec2f(vec.x.toDouble().pow(x.toDouble()).toFloat(), vec.y.toDouble().pow(x.toDouble()).toFloat())

internal fun normalize(vec: Vec2f): Vec2f {
  val f = 1.0f / length(vec)
  return Vec2f(vec.x * f, vec.y * f)
}

internal fun round(vec: Vec2f): Vec2f =
  Vec2f(round(vec.x), round(vec.y))

internal fun floor(vec: Vec2f): Vec2f =
  Vec2f(floor(vec.x), floor(vec.y))

internal fun ceil(vec: Vec2f): Vec2f =
  Vec2f(ceil(vec.x), ceil(vec.y))

internal fun min(a: Vec2f, b: Vec2f): Vec2f =
  Vec2f(min(a.x, b.x), min(a.y, b.y))

internal fun min(a: Vec2i, b: Vec2i): Vec2i =
  Vec2i(min(a.x, b.x), min(a.y, b.y))

internal fun max(a: Vec2f, b: Vec2f): Vec2f =
  Vec2f(max(a.x, b.x), max(a.y, b.y))

internal fun max(a: Vec2i, b: Vec2i): Vec2i =
  Vec2i(max(a.x, b.x), max(a.y, b.y))
