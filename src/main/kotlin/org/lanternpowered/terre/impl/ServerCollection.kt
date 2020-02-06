/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerCollection
import org.lanternpowered.terre.ServerInfo
import java.util.concurrent.ConcurrentHashMap

internal class ServerCollectionImpl(
    private val map: MutableMap<String, ServerImpl> = ConcurrentHashMap()
) : ServerCollection, Collection<Server> by map.values {

  override fun get(name: String): Server? = this.map[name.toLowerCase()]

  override fun register(serverInfo: ServerInfo): ServerImpl {
    val key = serverInfo.name.toLowerCase()
    val server = ServerImpl(serverInfo)
    val previous = this.map.putIfAbsent(key, server)
    check(previous == null) {
      "A server already exists with the name: ${serverInfo.name}" }
    return server
  }

  fun unregister(server: Server) {
    this.map.remove(server.info.name.toLowerCase(), server)
  }
}
