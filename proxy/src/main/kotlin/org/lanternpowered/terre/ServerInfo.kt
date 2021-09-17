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

import java.net.InetSocketAddress

/**
 * Represents a server that a player can connect to.
 *
 * If [protocolVersion] is null, the proxy will attempt to connect with multiple versions. This
 * has the benefit that you can swap the server without having to modify the info, but the first
 * time someone connects to the server can take a bit longer. After the server has discovered the
 * version there shouldn't be a difference in connecting time.
 *
 * @property name The name of the server, which should be human-readable
 * @property address The address of the server where the proxy should connect to
 * @property password The password of the server, if required
 * @property protocolVersion The protocol version that is used by the server, or null if unknown
 */
data class ServerInfo(
  val name: String,
  val address: InetSocketAddress,
  val password: String = "",
  val protocolVersion: ProtocolVersion? = null
) {

  init {
    check(name.isNotBlank()) { "The server name cannot be blank" }
  }
}
