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

import java.util.Objects

/**
 * Represents a range of [Version]s.
 *
 * @property start The start of the range
 * @property end The end of the range (inclusive)
 */
class VersionRange {

  val start: Version
  val end: Version

  constructor(start: Version, end: Version) {
    this.start = if (start < end) start else end
    this.end = if (start < end) end else start
  }

  constructor(first: Int, second: IntRange = 0..Int.MAX_VALUE) {
    this.start = Version(first, second.first)
    this.end = Version(first, second.last)
  }

  constructor(first: Int, second: Int, third: IntRange = 0..Int.MAX_VALUE) {
    this.start = Version(first, second, third.first)
    this.end = Version(first, second, third.last)
  }

  constructor(first: Int, second: Int, third: Int, fourth: IntRange = 0..Int.MAX_VALUE) {
    this.start = Version(first, second, third, fourth.first)
    this.end = Version(first, second, third, fourth.last)
  }

  operator fun contains(version: Version)
      = version >= this.start && version <= this.end

  override fun equals(other: Any?)
      = other is VersionRange && other.start == this.start && other.end == this.end

  override fun hashCode()
      = Objects.hash(this.start, this.end)

  override fun toString() = "VersionRange(start=$start,end=$end)"
}
