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
import org.lanternpowered.terre.impl.event.TerreEventBus
import kotlin.reflect.KClass

/**
 * Subscribes to events with the given event type.
 */
inline fun <reified T : Event> EventBus.subscribe(
  order: Int = Order.Normal, noinline listener: suspend (event: T) -> Unit
): EventSubscription {
  return subscribe(T::class, order, listener)
}

/**
 * A bus that can be used to post events and listen to them.
 */
interface EventBus {

  /**
   * Subscribes to events with the given event type.
   */
  fun <T : Event> subscribe(
    eventType: KClass<T>, order: Int = Order.Normal, listener: suspend (event: T) -> Unit
  ) : EventSubscription

  /**
   * Subscribes to events of annotated methods within the listener class.
   */
  fun register(listener: Any): ListenerRegistration

  /**
   * Unregisters listeners for the given listener instance.
   */
  fun unregister(listener: Any)

  /**
   * Posts an [Event] to this event bus.
   */
  suspend fun <T : Event> post(event: T)

  /**
   * Posts an [Event] to this event bus.
   */
  fun <T : Event> postAndForget(event: T)

  /**
   * Posts an async [Event] to this event bus.
   */
  fun <T : Event> postAsync(event: T): Deferred<T>

  /**
   * The singleton instance of the event bus.
   */
  companion object : EventBus by TerreEventBus
}
