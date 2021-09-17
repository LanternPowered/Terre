/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

internal fun calculateLength(fn: PacketLengthBuilder.() -> Unit): Int {
  return PacketLengthBuilderImpl().also(fn).length
}

internal interface PacketLengthBuilder {
  fun int()
  fun byte()
  fun playerId()
  fun short()
  fun long()
  fun double()
  fun float()
  fun vec2f()
  fun vec2i()
  fun shortVec2i()
}

private class PacketLengthBuilderImpl : PacketLengthBuilder {

  var length = 0

  override fun int() {
    length += Int.SIZE_BYTES
  }

  override fun byte() {
    length += Byte.SIZE_BYTES
  }

  override fun playerId() {
    byte()
  }

  override fun short() {
    length += Short.SIZE_BYTES
  }

  override fun long() {
    length += Long.SIZE_BYTES
  }

  override fun double() {
    long()
  }

  override fun float() {
    int()
  }

  override fun vec2f() {
    long() // 2 Floats
  }

  override fun vec2i() {
    long() // 2 Ints
  }

  override fun shortVec2i() {
    int() // 2 Shorts
  }
}
