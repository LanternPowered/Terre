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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.lanternpowered.terre.ProtocolVersion

internal object ProtocolRegistry {

  private val mutableTranslations = mutableListOf<ProtocolTranslation>()
  private val byId = Int2ObjectOpenHashMap<VersionedProtocol>()

  /**
   * All the allowed protocol translations.
   */
  val allowedTranslations: List<ProtocolTranslation>
    get() = mutableTranslations

  init {
    // TODO: Are there differences between 270, 273 and 274? if so create protocol for 1.4.4.1
    //  and/or 1.4.4.4
    register(ProtocolVersion.Vanilla.`1․4․4․1`, Protocol274) // 270
    register(ProtocolVersion.Vanilla.`1․4․4․4`, Protocol274) // 273
    register(ProtocolVersion.Vanilla.`1․4․4․5`, Protocol274) // 274
  }

  val all: Collection<VersionedProtocol> get() = byId.values

  operator fun get(id: Int): MultistateProtocol? = byId[id]?.protocol

  /**
   * Attempts to get the [protocol] instance for the
   * provided [ProtocolVersion].
   */
  operator fun get(version: ProtocolVersion): MultistateProtocol? {
    if (version is ProtocolVersion.Vanilla)
      return get(version.protocol)

    // TODO: Change version ranges for modded, etc.
    return null // Protocol194
  }

  /**
   * Registers a new protocol version.
   */
  private fun register(version: ProtocolVersion, protocol: MultistateProtocol) {
    check(version is ProtocolVersion.Vanilla) // TODO: Modded
    check(version.protocol !in this.byId) {
      "Protocol version ${version.protocol} is already in use." }
    byId[version.protocol] = VersionedProtocol(version, protocol)
    mutableTranslations += ProtocolTranslation(protocol, protocol)
  }

  /**
   * Allows packets from one version to be translated to another one.
   */
  private fun allowTranslation(pair: Pair<MultistateProtocol, MultistateProtocol>) {
    mutableTranslations += ProtocolTranslation(pair.first, pair.second)
  }
}
