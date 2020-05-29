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
import org.lanternpowered.terre.math.Vec2i
import kotlin.test.assertEquals

class Vec2iTest {

  @Test fun `test vec2i construction`() {
    val x = 15148
    val y = 91234
    val vec = Vec2i(x, y)
    assertEquals(x, vec.x)
    assertEquals(y, vec.y)
  }

  @Test fun `test vec2i construction with negative values`() {
    val x = 158914
    val y = -86534
    val vec = Vec2i(x, y)
    assertEquals(x, vec.x)
    assertEquals(y, vec.y)
  }
}
