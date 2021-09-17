/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.event

import org.lanternpowered.terre.plugin.PluginContainer

internal class RegisteredHandler(
  val plugin: PluginContainer?,
  val order: Int,
  val eventType: Class<*>,
  val instance: Any,
  val handler: EventHandler
)
