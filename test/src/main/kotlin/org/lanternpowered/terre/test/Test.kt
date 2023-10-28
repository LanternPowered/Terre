/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.test

import org.lanternpowered.terre.Team
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.chat.ServerChatEvent
import org.lanternpowered.terre.event.connection.PlayerPostLoginEvent
import org.lanternpowered.terre.event.player.PlayerChangePvPEnabledEvent
import org.lanternpowered.terre.event.player.PlayerChangeTeamEvent
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.server.PlayerJoinServerEvent
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.inject
import org.lanternpowered.terre.text.LocalizedText
import org.lanternpowered.terre.text.text

@Plugin(id = "test")
class Test {

  private val logger = inject<Logger>()

  @Subscribe
  fun onInit(event: ProxyInitializeEvent) {
    logger.info("Initializing test plugin!")
  }

  @Subscribe
  fun onPlayerPostLogin(event: PlayerPostLoginEvent) {
    val player = event.player
    player.sendMessage("You joined.")
    player.team = Team.Pink
  }

  private val teamMessageCodes = listOf(13, 14, 15, 16, 17, 22).map { "LegacyMultiplayer.$it" }

  @Subscribe
  fun onServerChat(event: ServerChatEvent) {
    val message = event.message
    if (message is LocalizedText && message.key in teamMessageCodes)
      event.cancelled = true
  }

  /*
  @Subscribe
  fun onPlayerJoinServer(event: PlayerJoinServerEvent) {
    if (event.server.name == "lobby") {
      event.player.team = Team.Pink
    } else if (event.server.name == "survival") {
      event.player.team = Team.Yellow
    }
  }
  */

  @Subscribe
  fun onChangeTeam(event: PlayerChangeTeamEvent) {
    val player = event.player
    val from = player.team
    val to = event.team
    player.sendMessage("You switched from team ".text() + from.name.text(from.color) + " to "
      .text() + to.name.text(to.color) + ".".text())
  }

  @Subscribe
  fun onChangePvP(event: PlayerChangePvPEnabledEvent) {
    val player = event.player
    player.sendMessage("You ${if (event.pvpEnabled) "enabled" else "disabled"} pvp.")
  }
}
