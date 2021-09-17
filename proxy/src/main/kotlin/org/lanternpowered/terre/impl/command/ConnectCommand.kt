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
    val server = Proxy.servers[name]
    if (server != null) {
      name = server.info.name
      send(textOf("Attempting to connect to $name."))
      player.connectToWithFuture(server)
        .whenComplete { result, _ ->
          val message = if (result != null) {
            when (result) {
              is ServerConnectionRequestResult.Success -> {
                textOf("Successfully connected to $name.")
              }
              is ServerConnectionRequestResult.Disconnected -> {
                textOf("Failed to connect to $name") +
                  (result.reason?.also { ": ".text() + it } ?: textOf())
              }
              is ServerConnectionRequestResult.AlreadyConnected -> {
                textOf("You're already connected to $name.")
              }
              is ServerConnectionRequestResult.ConnectionInProgress -> {
                textOf("You're already connecting to another server.")
              }
            }
          } else textOf("Failed to connect to $name.")
          send(message)
        }
    } else {
      send(textOf("The server $name doesn't exist."))
    }
  }
}
