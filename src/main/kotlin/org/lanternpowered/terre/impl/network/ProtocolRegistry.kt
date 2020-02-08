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
  private val byId = Int2ObjectOpenHashMap<MultistateProtocol>()

  /**
   * All the allowed protocol translations.
   */
  val allowedTranslations: List<ProtocolTranslation>
    get() = this.mutableTranslations

  init {
    register(Protocol155)
    register(Protocol194)

    // Allow 1.3.5.3 version to connect to the older 1.3.0.7 version,
    // this allows desktop client to join 1.3.0.7 servers, which includes
    // mobile servers.
    allowTranslation(Protocol194 to Protocol155)
  }

  val all: Collection<MultistateProtocol> get() = this.byId.values

  operator fun get(id: Int): MultistateProtocol? = this.byId[id]

  /**
   * Attempts to get the [Protocol] instance for the
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
  private fun register(protocol: MultistateProtocol) {
    check(protocol.version !in this.byId) {
      "Protocol version ${protocol.version} is already in use." }
    this.byId[protocol.version] = protocol
    this.mutableTranslations += ProtocolTranslation(protocol, protocol)
  }

  /**
   * Allows packets from one version to be translated to another one.
   */
  private fun allowTranslation(pair: Pair<MultistateProtocol, MultistateProtocol>) {
    this.mutableTranslations += ProtocolTranslation(pair.first, pair.second)
  }
}
