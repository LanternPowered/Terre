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

import org.junit.jupiter.api.Test
import org.lanternpowered.terre.math.Vec2f
import kotlin.test.assertEquals

class Vec2fTest {

  @Test fun `test vec2f construction`() {
    val x = 15148.2134f
    val y = 91234.96478f
    val vec = Vec2f(x, y)
    assertEquals(x, vec.x)
    assertEquals(y, vec.y)
  }

  @Test fun `test vec2f construction with negative values`() {
    val x = 987.3647f
    val y = -9532.36578f
    val vec = Vec2f(x, y)
    assertEquals(x, vec.x)
    assertEquals(y, vec.y)
  }

  @Test fun `test vec2f construction with NaN value`() {
    val x = 15148f
    val y = Float.NaN
    val vec = Vec2f(x, y)
    assertEquals(x, vec.x)
    assertEquals(y, vec.y)
  }

  @Test fun `test vec2f equality`() {
    val x = 15148.2134f
    val y = 91234.96478f
    val vec1 = Vec2f(x, y)
    val vec2 = Vec2f(x, y)
    assertEquals(vec1, vec2)
  }
}
