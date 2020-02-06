/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.config

import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.impl.util.parseInetAddress
import java.util.*

/**
 * Represents raw server info, parsed from the configuration file.
 *
 * @property name The name of the server
 * @property address The ip address and port to connect to
 * @property password The password, if any
 * @property `allow-auto-join` Whether players are allowed to automatically connect to the server
 */
data class RawServerInfo(
    val name: String,
    val address: String,
    val password: String = "",
    val `allow-auto-join`: Boolean = false
) {

  fun toServerInfo(): ServerInfo {
    val address = parseInetAddress(this.address)
    val name = if (this.name.isBlank()) UUID.randomUUID().toString().take(8) else this.name
    return ServerInfo(name, address, this.password)
  }

  companion object {

    fun fromServerInfo(serverInfo: ServerInfo): RawServerInfo {
      val address = serverInfo.address
      val value = if (address.isUnresolved) {
        address.toString()
      } else {
        address.address.hostAddress + ':' + address.port
      }
      return RawServerInfo(serverInfo.name, value, serverInfo.password)
    }
  }
}
