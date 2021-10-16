/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.util

import org.lanternpowered.terre.impl.math.max
import org.lanternpowered.terre.impl.math.min
import org.lanternpowered.terre.math.Vec2f
import java.util.Objects

/**
 * Represents a bounding box.
 *
 * @property min The minimum
 * @property max The maximum
 */
class AABB(min: Vec2f, max: Vec2f) {

  /**
   * The minimum of the bounding box.
   */
  val min = min(min, max)

  /**
   * The maximum of the bounding box.
   */
  val max = max(min, max)

  /**
   * The size of the bounding box.
   */
  val size by lazy { max - min }

  /**
   * The center of the bounding box.
   */
  val center by lazy { (max - min) / 2 + min }

  private var hashCode = 0

  /**
   * Gets whether the given position is within the bounding box.
   */
  operator fun contains(position: Vec2f): Boolean =
    contains(position.x, position.y)

  /**
   * Gets whether the given position is within the bounding box.
   */
  fun contains(x: Float, y: Float): Boolean {
    return x >= min.x && y >= min.y &&
      x <= max.x && y <= max.y
  }

  /**
   * Gets whether the this bounding box collides with the other one.
   */
  fun intersects(other: AABB): Boolean {
    return max.x >= other.min.x && other.max.x >= min.x &&
      max.y >= other.min.y && other.max.y >= min.y
  }

  /**
   * Gets a new bounding box with the given offset.
   */
  fun offset(offset: Vec2f): AABB =
    AABB(min + offset, max + offset)

  /**
   * Gets a new bounding box with the given offset.
   */
  fun offset(x: Float, y: Float): AABB =
    offset(Vec2f(x, y))

  /**
   * Gets a new bounding box with the given offset.
   */
  fun expand(amount: Vec2f): AABB =
    expand(amount.x, amount.y)

  /**
   * Expands this bounding box by the given amount.
   */
  fun expand(x: Float, y: Float): AABB {
    val v = Vec2f(x / 2, y / 2)
    return AABB(min - v, max + v)
  }

  override fun equals(other: Any?): Boolean =
    other is AABB && min == other.min && max == other.max

  override fun hashCode(): Int {
    if (hashCode == 0)
      hashCode = Objects.hash(min, max)
    return hashCode
  }

  override fun toString(): String = ToStringHelper(this)
    .add("min", min)
    .add("max", max)
    .toString()

  companion object {

    /**
     * Creates a bounding box with the given size at the origin.
     */
    fun centerSize(size: Vec2f): AABB {
      val v = size / 2
      return AABB(-v, v)
    }
  }
}
