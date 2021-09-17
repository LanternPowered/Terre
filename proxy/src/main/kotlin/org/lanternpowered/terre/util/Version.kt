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

import java.lang.NumberFormatException
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a version.
 */
class Version : Comparable<Version> {

  private val backing: IntArray

  /**
   * Gets an int array with all the version values.
   */
  val values: IntArray
    get() = this.backing.clone()

  /**
   * Constructs a new version from the given version string.
   *
   * @throws IllegalArgumentException If the version string is empty or uses an invalid format
   */
  constructor(version: String) {
    check(version.isNotBlank()) { "Version string cannot be blank." }
    val parts = version.split(".")
    backing = IntArray(parts.size)
    parts.forEachIndexed { index, s ->
      try {
        backing[index] = s.toInt()
      } catch (ex: NumberFormatException) {
        throw IllegalArgumentException("Invalid version string: $version, $s isn't an int.")
      }
    }
  }

  /**
   * Constructs a new version.
   */
  constructor(first: Int) {
    backing = IntArray(1)
    backing[0] = first
  }

  /**
   * Constructs a new version.
   */
  constructor(first: Int, second: Int) {
    backing = IntArray(2)
    backing[0] = first
    backing[1] = second
  }

  /**
   * Constructs a new version.
   */
  constructor(first: Int, second: Int, third: Int) {
    backing = IntArray(3)
    backing[0] = first
    backing[1] = second
    backing[2] = third
  }

  /**
   * Constructs a new version.
   */
  constructor(first: Int, second: Int, third: Int, fourth: Int) {
    backing = IntArray(4)
    backing[0] = first
    backing[1] = second
    backing[2] = third
    backing[3] = fourth
  }

  /**
   * Constructs a new version.
   */
  constructor(first: Int, second: Int, third: Int, fourth: Int, vararg more: Int) {
    backing = IntArray(more.size + 4)
    backing[0] = first
    backing[1] = second
    backing[2] = third
    backing[3] = fourth
    more.copyInto(destination = backing, destinationOffset = 4)
  }

  /**
   * Constructs a new version.
   */
  constructor(values: IntArray) {
    check(values.isNotEmpty()) { "At least one value must be present in the array." }
    backing = values.clone()
  }

  private fun getBacking() = backing
  private val toString by lazy { getBacking().joinToString(".") }

  /**
   * Compares this version to the other one.
   */
  override fun compareTo(other: Version): Int {
    val s1 = values.size
    val s2 = other.values.size

    val common = min(s1, s2)
    for (i in 0 until common) {
      val v = values[i].compareTo(other.values[i])
      if (v != 0)
        return v
    }
    for (i in common until max(s1, s2)) {
      val c1 = if (s1 > s2) values[i] else 0
      val c2 = if (s2 > s1) other.values[i] else 0

      val v = c1.compareTo(c2)
      if (v != 0)
        return v
    }
    return 0
  }

  override fun equals(other: Any?) =
    other === this || (other is Version && compareTo(other) == 0)

  override fun hashCode() = values.contentHashCode()

  override fun toString() = toString
}
