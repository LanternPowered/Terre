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
import java.util.Comparator

internal object ProtocolRegistry {

  private val mutableTranslations = mutableListOf<ProtocolTranslation>()
  private val byId = Int2ObjectOpenHashMap<Protocol>()

  /**
   * All the allowed protocol translations.
   */
  val allowedTranslations: List<ProtocolTranslation>
    get() = this.mutableTranslations

  init {
    register(Protocol155)
    register(Protocol194)

    // Allow 1.3.0.7 version to connect to the newer 1.3.5.3 version,
    // this allows mobile clients to connect to a desktop version server.
    allowTranslation(Protocol155 to Protocol194)
    allowTranslation(Protocol194 to Protocol155)
  }

  val all: Collection<Protocol> get() = this.byId.values

  operator fun get(id: Int): Protocol? = this.byId[id]

  /**
   * Registers a new protocol version.
   */
  private fun register(protocol: Protocol) {
    check(protocol.version !in this.byId) {
      "Protocol version ${protocol.version} is already in use." }
    this.byId[protocol.version] = protocol
  }

  /**
   * Allows packets from one version to be translated to another one.
   */
  private fun allowTranslation(pair: Pair<Protocol, Protocol>) {
    this.mutableTranslations += ProtocolTranslation(pair.first, pair.second)
  }
}
