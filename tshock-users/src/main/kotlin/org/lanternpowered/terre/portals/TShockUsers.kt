/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.portals

import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.connection.ClientPreLoginEvent
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.plugin.Plugin

/**
 * A plugin which hooks into a tShock user system. This provides permissions for the proxy using
 * a tShock database and automatically login users to their backing tShock servers when switching
 * between them.
 *
 * This plugin is a must if you are using the user and permission system provided by tShock.
 */
@Plugin(id = "tshock-users")
object TShockUsers {

  @Subscribe
  private fun onInit(event: ProxyInitializeEvent) {

  }

  @Subscribe
  private fun onShutdown(event: ProxyShutdownEvent) {

  }

  @Subscribe
  private fun onClientConnect(event: ClientPreLoginEvent) {

  }
}
