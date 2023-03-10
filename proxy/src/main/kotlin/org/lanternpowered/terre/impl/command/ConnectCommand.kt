/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.command

import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.ServerConnectionRequestResult
import org.lanternpowered.terre.command.CommandSource
import org.lanternpowered.terre.command.SimpleCommandExecutor
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.text.textOf

internal object ConnectCommand : SimpleCommandExecutor {

  override suspend fun execute(source: CommandSource, alias: String, args: List<String>) {
    val player = source as? PlayerImpl ?: return
    var name = args.getOrNull(0)
    fun send(text: Text) {
      player.sendMessage(Terre.message(text))
    }
    if (name == null) {
      send("Please specify a target server.".text())
      return
    }
    if (!player.hasPermission("terre.command.connect.$name")) {
      send("You don't have permission to connect to ".text() + name.text(Terre.color) + ".".text())
      return
    }
    val server = Proxy.servers[name]
    if (server != null) {
      name = server.info.name
      player.connectToWithFuture(server)
        .whenComplete { result, _ ->
          val message = if (result != null) {
            when (result) {
              is ServerConnectionRequestResult.Success -> {
                "Successfully connected to ".text() + name.text(Terre.color) + ".".text()
              }
              is ServerConnectionRequestResult.Disconnected -> {
                "Failed to connect to ".text() + name.text(Terre.color) +
                  (result.reason?.also { ": ".text() + it } ?: textOf())
              }
              is ServerConnectionRequestResult.AlreadyConnected -> {
                "You're already connected to ".text() + name.text(Terre.color) + ".".text()
              }
              is ServerConnectionRequestResult.ConnectionInProgress -> {
                "You're already connecting to another server.".text()
              }
            }
          } else "Failed to connect to ".text() + name.text(Terre.color) + ".".text()
          send(message)
        }
    } else {
      send("The server ".text() + name.text(Terre.color) + " doesn't exist.".text())
    }
  }
}
