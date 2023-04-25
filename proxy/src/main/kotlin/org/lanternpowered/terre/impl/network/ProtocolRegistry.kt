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
    register(ProtocolVersion.Vanilla.`1․4․0․5`, Protocol230) // 230
    register(ProtocolVersion.Vanilla.`1․4․2․3`, Protocol238) // 238
    register(ProtocolVersion.Vanilla.`1․4․3․6`, Protocol248) // 248
    register(ProtocolVersion.Vanilla.`1․4․4․1`, Protocol274) // 270
    register(ProtocolVersion.Vanilla.`1․4․4․4`, Protocol274) // 273
    register(ProtocolVersion.Vanilla.`1․4․4․5`, Protocol274) // 274
    register(ProtocolVersion.Vanilla.`1․4․4․9`, Protocol274) // 279

    allowTranslation(Protocol238 to Protocol230)
  }

  val all: Collection<VersionedProtocol> get() = byId.values

  operator fun get(id: Int): Protocol? = byId[id]?.protocol

  /**
   * Attempts to get the [protocol] instance for the provided [ProtocolVersion].
   */
  operator fun get(version: ProtocolVersion): Protocol? {
    if (version is ProtocolVersion.Vanilla)
      return get(version.protocol)

    // TODO: Change version ranges for modded, etc.
    return null // Protocol194
  }

  /**
   * Registers a new protocol version.
   */
  private fun register(version: ProtocolVersion, protocol: Protocol) {
    check(version is ProtocolVersion.Vanilla) // TODO: Modded
    check(version.protocol !in this.byId) {
      "Protocol version ${version.protocol} is already in use." }
    byId[version.protocol] = VersionedProtocol(version, protocol)
    mutableTranslations += ProtocolTranslation(protocol, protocol)
  }

  /**
   * Allows packets from one version to be translated to another one.
   */
  private fun allowTranslation(pair: Pair<Protocol, Protocol>) {
    mutableTranslations += ProtocolTranslation(pair.first, pair.second)
  }
}
