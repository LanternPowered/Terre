/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre

/**
 * Represents a collection of [Server] instances.
 */
interface ServerCollection : Collection<Server> {

  /**
   * Attempts to get a [Server] from the given [name]. Retrieving by name is case-insensitive.
   */
  operator fun get(name: String): Server?

  /**
   * Attempts to register a new [Server] with the given [ServerInfo]. If a server with the same
   * name is already registered (case-insensitive), this will result in an
   * [IllegalArgumentException].
   *
   * If a [ProtocolVersion] is provided, and it is not supported by the proxy, an
   * [IllegalArgumentException] is expected.
   */
  fun register(serverInfo: ServerInfo): Server
}
