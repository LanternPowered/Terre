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
    this.length += Int.SIZE_BYTES
  }

  override fun byte() {
    this.length += Byte.SIZE_BYTES
  }

  override fun short() {
    this.length += Short.SIZE_BYTES
  }

  override fun long() {
    this.length += Long.SIZE_BYTES
  }

  override fun double() {
    this.length += Long.SIZE_BYTES
  }

  override fun float() {
    this.length += Int.SIZE_BYTES
  }

  override fun vec2f() {
    this.length += Long.SIZE_BYTES // 2 Floats
  }

  override fun vec2i() {
    this.length += Long.SIZE_BYTES // 2 Ints
  }

  override fun shortVec2i() {
    this.length += Int.SIZE_BYTES // 2 Shorts
  }
}
