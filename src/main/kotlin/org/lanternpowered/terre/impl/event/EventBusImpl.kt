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

import kotlinx.coroutines.Deferred
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.plugin.PluginContainer
import kotlin.reflect.KClass

internal object EventBusImpl : EventBus() {

  override fun <T : Event> register(
      pluginContainer: PluginContainer, eventType: KClass<T>, order: Int, listener: suspend (event: T) -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Event> register(
      pluginContainer: PluginContainer, eventType: KClass<T>, order: Int, listener: (event: T) -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun <T : Event> postAsync(event: T): Deferred<T> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
