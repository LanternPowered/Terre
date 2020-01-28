/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.test

import org.apache.logging.log4j.Logger
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.plugin.inject

@Plugin(id = "test", name = "Test")
object TestPlugin {

  private val logger = inject<Logger>()
  private val pluginContainer = inject<PluginContainer>()
  private val eventBus = inject<EventBus>()

  @Subscribe private suspend fun onInitialize(event: ProxyInitializeEvent) {
    this.eventBus.post(InitEvent)
  }

  @Subscribe private fun onShutdown(event: ProxyShutdownEvent) {

  }

  /**
   * Custom event posted when the [TestPlugin] is initialized.
   */
  object InitEvent : Event
}
