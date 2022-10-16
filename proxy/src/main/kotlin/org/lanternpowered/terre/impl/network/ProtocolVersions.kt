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

import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.util.Version

internal object ProtocolVersions {

  /**
   * All the known protocol version numbers paired to their version name.
   */
  private val knownVanillaVersions = listOf(
    ProtocolVersion.Vanilla.`1․4․0․5`,
    ProtocolVersion.Vanilla.`1․4․2․3`,
    ProtocolVersion.Vanilla.`1․4․4․1`,
    ProtocolVersion.Vanilla.`1․4․4․4`,
    ProtocolVersion.Vanilla.`1․4․4․5`,
  ).associateBy { it.protocol }

  /**
   * Gets the vanilla [ProtocolVersion] for the given protocol version number.
   */
  operator fun get(protocol: Int) = knownVanillaVersions[protocol]

  /**
   * Gets the vanilla [ProtocolVersion] for the given protocol version number.
   */
  operator fun get(version: String): ProtocolVersion.Vanilla? {
    val v = Version(version)
    return knownVanillaVersions.asSequence().firstOrNull { v == it.value.version }?.value
  }
}
