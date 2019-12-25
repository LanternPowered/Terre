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

  private val byId = Int2ObjectOpenHashMap<Protocol>()

  init {
    register(Protocol155)
    register(Protocol194)
  }

  /**
   * Gets the latest supported protocol version.
   */
  val latest: Protocol
    get() = this.byId.int2ObjectEntrySet().stream()
        .sorted(Comparator.comparing { -it.intKey }) // Reverse
        .findFirst()
        .get()
        .value

  val all: Collection<Protocol> get() = this.byId.values

  operator fun get(id: Int): Protocol? = this.byId[id]

  private fun register(protocol: Protocol) {
    check(protocol.version !in this.byId) {
      "Protocol version ${protocol.version} is already in use." }
    this.byId[protocol.version] = protocol
  }
}
