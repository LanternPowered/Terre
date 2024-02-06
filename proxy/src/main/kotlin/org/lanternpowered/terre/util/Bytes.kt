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

import java.util.function.Consumer

fun ByteArray.toBytes(): Bytes = Bytes.wrap(copyOf())

/**
 * An immutable wrapper around a byte array.
 */
class Bytes private constructor(private val array: ByteArray) : Iterable<Byte> {

  private var hashCode = 0

  override fun iterator() = array.iterator()

  override fun forEach(action: Consumer<in Byte>) {
    super.forEach(action)
  }

  inline fun forEach(action: (value: Byte) -> Unit) {
    val itr = iterator()
    while (itr.hasNext()) {
      action(itr.next())
    }
  }

  fun isEmpty(): Boolean = array.isEmpty()

  fun isNotEmpty(): Boolean = array.isNotEmpty()

  fun toByteArray() = array.copyOf()

  internal fun unwrap() = array

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other !is Bytes) {
      return false
    }
    return array.contentEquals(other.array)
  }

  override fun hashCode(): Int {
    if (hashCode == 0) {
      hashCode = array.contentHashCode()
    }
    return hashCode
  }

  override fun toString(): String {
    return "Bytes(${array.contentToString()})"
  }

  companion object {

    val Empty = Bytes(byteArrayOf())

    internal fun wrap(array: ByteArray): Bytes {
      if (array.isEmpty()) {
        return Empty
      }
      return Bytes(array)
    }
  }
}
