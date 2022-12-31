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
  private val knownVanillaVersions = ProtocolVersion.Vanilla.Companion::class
    .members
    .asSequence()
    .filter { member -> member.returnType.classifier == ProtocolVersion.Vanilla::class }
    .map { member -> member.call(ProtocolVersion.Vanilla.Companion) as ProtocolVersion.Vanilla }
    .associateBy { it.protocol }

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
