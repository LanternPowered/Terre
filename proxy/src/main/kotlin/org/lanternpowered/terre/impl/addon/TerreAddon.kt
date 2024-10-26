/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.addon

object TerreAddon {

  val mod = requireNotNull(TerreAddon::class.java.getResourceAsStream("/TerreAddon.tmod"))
    .use { input -> Mod.load(input) }
}
