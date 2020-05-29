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

import com.uchuhimo.konf.ConfigSpec
import org.lanternpowered.terre.impl.Terre

internal object ProxyConfigSpec : ConfigSpec("proxy") {

  val name by optional(default = Terre.name,
      description = "The name of the proxy.")

  val host by optional(default = "0.0.0.0",
      description = "The host address the server will be bound to.")

  val port by optional(default = 7777,
      description = "The port the server will be bound to.")

  val password by optional(default = "",
      description = "The password that is required to join. Leave empty to disable.")

  val maxPlayers by optional(name = "max-players", default = -1,
      description = "The maximum amount of players that are allowed to join simultaneously. If set to -1, " +
          "players can join as long that backing servers have empty slots.")

  val servers by optional(
      default = listOf(
          RawServerInfo(name = "lobby", address = "127.0.0.1:7778", password = "", `allow-auto-join` = true),
          RawServerInfo(name = "survival", address = "127.0.0.1:7779", password = "", `allow-auto-join` = false)
      ),
      description = """
        The servers that can be connected to through the proxy. When adding a server it's required to specify 
        the name and address to connect to it. And if necessary a password. Setting allow-auto-join to true
        allows players to automatically connect to this server when connecting to the proxy for the first time.
      """.trimIndent())
}
