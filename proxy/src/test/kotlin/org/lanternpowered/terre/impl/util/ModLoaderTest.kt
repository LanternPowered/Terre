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

import org.lanternpowered.terre.impl.addon.TerreAddon
import kotlin.test.Test
import kotlin.test.assertEquals

class ModLoaderTest {

  @Test fun test() {
    assertEquals("TerreAddon", TerreAddon.mod.name)
  }
}
