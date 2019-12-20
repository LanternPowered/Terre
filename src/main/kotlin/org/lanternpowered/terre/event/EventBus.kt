/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event

import kotlinx.coroutines.Deferred
import org.lanternpowered.terre.plugin.PluginContainer
import kotlin.reflect.KClass

/**
 * A bus that can be used to post events and listen to them.
 */
abstract class EventBus {

  /**
   * Subscribes to events with the given event type.
   */
  inline fun <reified T : Event> register(
      pluginContainer: PluginContainer = PluginContainer.Active,
      order: Int = Order.Normal, noinline listener: suspend (event: T) -> Unit) {
    register(pluginContainer, T::class, order, listener)
  }

  /**
   * Subscribes to events with the given event type.
   */
  inline fun <reified T : Event> register(
      pluginContainer: PluginContainer = PluginContainer.Active,
      order: Int = Order.Normal, noinline listener: (event: T) -> Unit) {
    register(pluginContainer, T::class, order, listener)
  }

  /**
   * Subscribes to events with the given event type.
   */
  abstract fun <T : Event> register(
      pluginContainer: PluginContainer = PluginContainer.Active,
      eventType: KClass<T>, order: Int = Order.Normal, listener: suspend (event: T) -> Unit
  )

  /**
   * Subscribes to events with the given event type.
   */
  abstract fun <T : Event> register(
      pluginContainer: PluginContainer = PluginContainer.Active,
      eventType: KClass<T>, order: Int = Order.Normal, listener: (event: T) -> Unit
  )

  /**
   * Posts an [Event] to this event bus.
   */
  suspend fun <T : Event> post(event: T) = postAsync(event).await()

  /**
   * Posts an async [Event] to this event bus.
   */
  abstract fun <T : Event> postAsync(event: T): Deferred<T>
}
