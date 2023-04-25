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
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.event.server.ServerRegisterEvent
import org.lanternpowered.terre.event.server.ServerUnregisterEvent
import org.lanternpowered.terre.impl.network.ProtocolRegistry
import org.lanternpowered.terre.impl.network.VersionedProtocol
import java.util.concurrent.ConcurrentHashMap

internal class ServerCollectionImpl(
  private val map: MutableMap<String, ServerImpl> = ConcurrentHashMap()
) : ServerCollection, Collection<Server> by map.values {

  override fun get(name: String): Server? = map[name.lowercase()]

  override fun register(serverInfo: ServerInfo): ServerImpl =
    register(serverInfo, silently = false)

  fun register(serverInfo: ServerInfo, silently: Boolean = false): ServerImpl {
    val key = serverInfo.name.lowercase()
    val version = serverInfo.protocolVersion
    val protocol = if (version != null) {
      val protocol = ProtocolRegistry[version]
      checkNotNull(protocol) {
        "The provided protocol version isn't supported: $version" }
      VersionedProtocol(version, protocol)
    } else null
    val server = ServerImpl(serverInfo, false, protocol)
    synchronized(server.registerLock) {
      val previous = map.putIfAbsent(key, server)
      check(previous == null) {
        "A server already exists with the name: ${serverInfo.name}" }
      server.init()
      if (!silently)
        EventBus.postAndForget(ServerRegisterEvent(server))
    }
    return server
  }

  fun unregister(server: Server) {
    server as ServerImpl
    synchronized(server.registerLock) {
      if (!map.remove(server.info.name.lowercase(), server))
        return
      EventBus.postAndForget(ServerUnregisterEvent(server))
    }
  }
}
