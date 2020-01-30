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

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.google.common.collect.HashMultimap
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lanternpowered.lmbda.kt.createLambda
import org.lanternpowered.lmbda.kt.lambdaType
import org.lanternpowered.lmbda.kt.privateLookupIn
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.event.EventSubscription
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.plugin.ActivePluginThreadLocalElement
import org.lanternpowered.terre.impl.plugin.activePlugin
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction

internal object TerreEventBus : EventBus {

  private val lock = Any()

  private val coroutineScope = CoroutineScope(EventExecutor.dispatcher)

  private val handlersByEvent = HashMultimap.create<Class<*>, RegisteredHandler>()
  private val handlersCache: LoadingCache<Class<*>, List<RegisteredHandler>>
      = Caffeine.newBuilder().initialCapacity(150).build(::bakeHandlers)

  private val methodHandlers: LoadingCache<Method, UntargetedEventHandler>
      = Caffeine.newBuilder().build(::buildMethodListener)

  private fun bakeHandlers(eventType: Class<*>): List<RegisteredHandler> {
    val baked = mutableListOf<RegisteredHandler>()
    val types = eventType.eventTypes

    synchronized(this.lock) {
      for (type in types) {
        baked += this.handlersByEvent.get(type)
      }
    }

    baked.sortBy { it.order }
    return baked
  }

  private fun getHandlers(eventType: Class<*>) =
      this.handlersCache.get(eventType) ?: error("Shouldn't be possible")

  private val Class<*>.eventTypes
    get() = TypeToken.of(this).types.rawTypes().filter { Event::class.java.isAssignableFrom(it) }

  override fun <T : Event> subscribe(
      eventType: KClass<T>, order: Int, listener: suspend (event: T) -> Unit
  ): EventSubscription {
    val handler = object : EventHandler {
      override suspend fun handle(event: Event) {
        @Suppress("UNCHECKED_CAST")
        listener(event as T)
      }
    }
    val plugin = activePlugin
    val registration = RegisteredHandler(plugin, order, eventType.java, listener, handler)
    register(listOf(registration))
    return object : EventSubscription {
      override fun unsubscribe() {
        unsubscribe(listener)
      }
    }
  }

  override fun subscribe(listener: Any): EventSubscription {
    val plugin = activePlugin
    val registrations = mutableListOf<RegisteredHandler>()
    val map = mutableMapOf<String, Pair<MethodListenerInfo, String?>>()
    collectEventMethods(listener.javaClass, map)

    for ((info, errors) in map.values) {
      if (errors != null) {
        Terre.logger.warn("Invalid listener method {} in {}: {}", info.method.name,
            info.method.declaringClass.name, errors)
        continue
      }

      val untargetedHandler = this.methodHandlers.get(info.method) ?: error("Shouldn't happen.")
      val handler = object : EventHandler {
        override suspend fun handle(event: Event) {
          untargetedHandler.handle(listener, event)
        }
      }

      registrations.add(RegisteredHandler(plugin, info.order, info.eventType, listener, handler))
    }

    register(registrations)
    return object : EventSubscription {
      override fun unsubscribe() {
        unsubscribe(listener)
      }
    }
  }

  override fun unsubscribe(listener: Any) {
    val removed = mutableListOf<RegisteredHandler>()
    synchronized(this.lock) {
      val it = this.handlersByEvent.values().iterator()
      while (it.hasNext()) {
        val handler = it.next()
        if (handler.instance === listener) {
          it.remove()
          removed += handler
        }
      }
    }

    // Invalidate the affected event types
    this.handlersCache.invalidateAll(removed.flatMap { it.eventType.eventTypes }.distinct())
  }

  private class MethodListenerInfo(
      val method: Method,
      val eventType: Class<*>,
      val order: Int
  )

  private fun collectEventMethods(type: Class<*>, map: MutableMap<String, Pair<MethodListenerInfo, String?>>) {
    for (method in type.declaredMethods) {
      if (method.isSynthetic)
        continue

      val annotation = method.getAnnotation(Subscribe::class.java) ?: continue
      val key = method.name + method.parameterTypes.joinToString(
          separator = ",", prefix = "(", postfix = ")")
      if (key in map)
        continue

      var eventType: Class<*> = Event::class.java
      val errors = mutableListOf<String>()
      if (Modifier.isStatic(method.modifiers))
        errors += "function must not be static"
      val function = method.kotlinFunction ?: error("Cannot get function for ${method.name}")
      if (function.isOperator)
        errors += "function must not be an operator"
      if (function.isInline)
        errors += "function must not be inlined"
      if (function.parameters.size != 2) {
        errors += "function must have 1 parameter which is the event"
      } else {
        eventType = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(eventType))
          errors += "parameter isn't an event"
        val parameter = function.parameters[1]
        if (parameter.isOptional)
          errors += "parameter can't be optional"
        if (parameter.isVararg)
          errors += "parameter can't be vararg"
      }

      val info = MethodListenerInfo(method, eventType, annotation.order)
      map[key] = info to if (errors.isEmpty()) null else errors.joinToString(", ")
    }
    for (itf in type.interfaces) {
      collectEventMethods(itf, map)
    }
    if (type.superclass != Any::class.java) {
      collectEventMethods(type.superclass, map)
    }
  }

  private val untargetedEventHandlerType = lambdaType<UntargetedEventHandler>()
  private val untargetedEventHandlerNoSuspendType = lambdaType<UntargetedEventHandler.NoSuspend>()

  private val methodHandlesLookup = MethodHandles.lookup()

  private fun buildMethodListener(method: Method): UntargetedEventHandler {
    val function = method.kotlinFunction ?: error("Cannot get function for ${method.name}")
    val methodHandle = this.methodHandlesLookup.privateLookupIn(method.declaringClass).unreflect(method)

    val handlerType
        = if (function.isSuspend) this.untargetedEventHandlerType else this.untargetedEventHandlerNoSuspendType
    return methodHandle.createLambda(handlerType)
  }

  private fun register(listeners: List<RegisteredHandler>) {
    synchronized(this.lock) {
      for (listener in listeners) {
        this.handlersByEvent.put(listener.eventType, listener)
      }
    }
    // Invalidate all the affected event subtypes
    this.handlersCache.invalidateAll(listeners.flatMap { it.eventType.eventTypes }.distinct())
  }

  override suspend fun <T : Event> post(event: T) {
    val handlers = getHandlers(event.javaClass)
    if (handlers.isEmpty()) {
      return
    }
    handleHandlers(event, handlers)
  }

  override fun <T : Event> postAndForget(event: T) {
    post(event) {}
  }

  override fun <T : Event> postAsync(event: T): Deferred<T> {
    val deferred = CompletableDeferred<T>()
    post(event, deferred::complete)
    return deferred
  }

  fun <T : Event> postAsyncWithFuture(event: T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    post(event, future::complete)
    return future
  }

  private inline fun <T : Event> post(event: T, crossinline complete: (T) -> Unit) {
    val listeners = this.getHandlers(event.javaClass)
    // Only launch a coroutine if necessary
    if (listeners.isEmpty()) {
      complete(event)
      return
    }
    this.coroutineScope.launch {
      handleHandlers(event, listeners)
      complete(event)
    }
  }

  private suspend fun handleHandlers(event: Event, handlers: List<RegisteredHandler>) {
    for (handler in handlers) {
      val plugin = handler.plugin
      try {
        withContext(ActivePluginThreadLocalElement(plugin)) {
          handler.handler.handle(event)
        }
      } catch (ex: Throwable) {
        Terre.logger.info("Couldn't pass ${event::class.simpleName}" +
            if (plugin == null) "" else " to ${plugin.id}", ex)
      }
    }
  }
}
