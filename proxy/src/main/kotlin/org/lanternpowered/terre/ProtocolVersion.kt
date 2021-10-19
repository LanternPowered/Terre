/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NonAsciiCharacters", "ObjectPropertyName")

package org.lanternpowered.terre

import org.lanternpowered.terre.util.Version

/**
 * Represents the protocol version.
 */
sealed class ProtocolVersion {

  /**
   * Represents a vanilla protocol version.
   */
  data class Vanilla(
    val version: Version,
    val protocol: Int
  ) : ProtocolVersion(), Comparable<Vanilla> {

    constructor(version: String, protocol: Int) : this(Version(version), protocol)

    override fun compareTo(other: Vanilla) =
      protocol.compareTo(other.protocol)

    override fun equals(other: Any?) =
      other is Vanilla && other.protocol == protocol

    override fun hashCode() = protocol.hashCode()

    companion object {

      /**
       * The 1.4.0.5 vanilla version.
       */
      val `1․4․0․5` = Vanilla("1.4.0.5", 230)

      /**
       * The 1.4.2.3 vanilla version.
       */
      val `1․4․2․3` = Vanilla("1.4.2.3", 238)
    }
  }

  /**
   * Represents a tModLoader protocol version.
   *
   * @property version The base version, e.g. 0.11.6.2
   * @property branch The branch, if not the default
   * @property beta The beta build number, if applicable
   */
  data class TModLoader(
    val version: Version,
    val branch: String? = null,
    val beta: Int? = null
  ) : ProtocolVersion(), Comparable<TModLoader> {

    override fun compareTo(other: TModLoader) =
      compareValuesBy(this, other, { it.version }, { it.branch }, { it.beta })
  }
}
