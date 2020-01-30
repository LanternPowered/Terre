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
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf

internal object ServerConfigSpec : ConfigSpec("server") {

  val host by optional("0.0.0.0",
      description = "The host address the server will be bound to.")
  val port by optional(7777,
      description = "The port the server will be bound to.")

  val password by optional("",
      description = "The password that is required to join. Leave empty to disable.")

  val maxPlayers by optional(-1, name = "max-players",
      description = "The maximum amount of players that are allowed to join simultaneously. If set to -1, " +
          "players can join as long that backing servers have empty slots.")

  val messageOfTheDay by optional<Text>(textOf("This is a Terre server!"), name = "message-of-the-day")
}
