/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util

import org.junit.jupiter.api.Test
import org.lanternpowered.terre.util.Version
import kotlin.test.assertEquals

class VersionTest {

  @Test fun `test-version-equality`() {
    val a = Version(0, 10, 5, 3)
    val b = Version(0, 10, 5, 3)

    assertEquals(a, b)
    assertEquals(0, a.compareTo(b))
  }

  @Test fun `test-version-comparison`() {
    val a = Version(0, 10, 5, 3)
    val b = Version(0, 11, 5, 3)
    val c = Version(0, 11, 5, 7)

    assert(a < b)
    assert(c > b)
  }

  @Test fun `test-version-comparison-unequal-length`() {
    val a = Version(0, 11, 5, 3)
    val b = Version(0, 11, 5, 3, 10)

    assert(b > a)
  }
}
