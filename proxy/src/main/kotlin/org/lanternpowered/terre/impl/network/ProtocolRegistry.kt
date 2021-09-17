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
    register(ProtocolVersion.Vanilla.`1․3․0․7`, Protocol155)
    register(ProtocolVersion.Vanilla.`1․3․0․8`, Protocol155)
    register(ProtocolVersion.Vanilla.`1․3․5․3`, Protocol194)
    register(ProtocolVersion.Vanilla.`1․4․0․5`, Protocol230)

    // Allow 1.3.5.3 version to connect to the older 1.3.0.7 version,
    // this allows desktop client to join 1.3.0.7 servers, which includes
    // mobile servers.
    allowTranslation(Protocol194 to Protocol155)
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
