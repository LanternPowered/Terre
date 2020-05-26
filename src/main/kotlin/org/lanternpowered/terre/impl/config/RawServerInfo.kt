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
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.network.ProtocolVersions
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
    val `allow-auto-join`: Boolean = false,
    val protocol: String = ""
) {

  fun toServerInfo(): ServerInfo {
    val address = parseInetAddress(
        if (':' in this.address) this.address else "$address:7777")
    val name = if (this.name.isBlank()) UUID.randomUUID().toString().take(8) else this.name
    val version = if (this.protocol.isBlank()) null else {
      val number = this.protocol.toIntOrNull()
      val version = if (number != null) {
        ProtocolVersions[number]
      } else {
        try {
          ProtocolVersions[this.protocol]
        } catch (ex: IllegalArgumentException) {
          null
        }
      }
      // TODO: Modded
      if (version == null) {
        Terre.logger.error("Found invalid vanilla protocol version \"$protocol\" while parsing server \"$name\".")
      }
      version
    }
    return ServerInfo(name, address, this.password, version)
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
