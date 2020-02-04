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
 * Represents a server that a player can
 * connect to.
 *
 * @property name The name of the server, which should be human readable
 * @property address The address of the server where the proxy should connect to
 * @property password The password of the server, if required
 */
data class ServerInfo(
    val name: String,
    val address: InetSocketAddress,
    val password: String = ""
) {

  init {
    check(this.name.isNotBlank()) { "The server name cannot be blank" }
  }
}
