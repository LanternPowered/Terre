/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.protocol

import io.netty.buffer.Unpooled
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.readVec2i
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.buffer.writeVec2i
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.math.Vec2i
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ByteBufTest {

  @Test
  fun writeVec2f() {
    val vec = Vec2f(125.0f, 26669.016f)

    val expected = Unpooled.buffer()
    expected.writeFloatLE(vec.x)
    expected.writeFloatLE(vec.y)

    val written = Unpooled.buffer()
    written.writeVec2f(vec)

    assertContentEquals(expected.array(), written.array())
  }

  @Test
  fun readVec2f() {
    val vec = Vec2f(125.0f, 26669.016f)

    val twoFloats = Unpooled.buffer()
    twoFloats.writeFloatLE(vec.x)
    twoFloats.writeFloatLE(vec.y)

    val read = twoFloats.readVec2f()

    assertEquals(vec, read)
  }


  @Test
  fun writeVec2i() {
    val vec = Vec2i(12369, -65891)

    val expected = Unpooled.buffer()
    expected.writeIntLE(vec.x)
    expected.writeIntLE(vec.y)

    val written = Unpooled.buffer()
    written.writeVec2i(vec)

    assertContentEquals(expected.array(), written.array())
  }

  @Test
  fun readVec2i() {
    val vec = Vec2i(12369, -65891)

    val twoFloats = Unpooled.buffer()
    twoFloats.writeIntLE(vec.x)
    twoFloats.writeIntLE(vec.y)

    val read = twoFloats.readVec2i()

    assertEquals(vec, read)
  }
}
