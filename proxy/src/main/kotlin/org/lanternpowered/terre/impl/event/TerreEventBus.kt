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
import org.lanternpowered.terre.event.ListenerRegistration
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.plugin.PluginThreadLocalElement
import org.lanternpowered.terre.impl.plugin.activePlugin
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.javaMethod

internal object TerreEventBus : EventBus {

  private val lock = ReentrantReadWriteLock()

  private val coroutineScope = CoroutineScope(EventExecutor.dispatcher)

  private val handlersByEvent = HashMultimap.create<Class<*>, RegisteredHandler>()
  private val handlersCache: LoadingCache<Class<*>, List<RegisteredHandler>> =
    Caffeine.newBuilder().initialCapacity(150).build(::bakeHandlers)

  private val methodHandlers: LoadingCache<MethodInfo, UntargetedEventHandler> =
    Caffeine.newBuilder().build(::buildMethodListener)

  private data class MethodInfo(
    val method: Method,
    val isSuspend: Boolean
  )

  private fun bakeHandlers(eventType: Class<*>): List<RegisteredHandler> {
    val baked = mutableListOf<RegisteredHandler>()
    val types = eventType.eventTypes

    lock.read {
      for (type in types) {
        baked += handlersByEvent.get(type)
      }
    }

    baked.sortBy { it.order }
    return baked
  }

  private fun getHandlers(eventType: Class<*>) =
    handlersCache.get(eventType) ?: error("Shouldn't be possible")

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
        unregister(listener)
      }
    }
  }

  override fun register(listener: Any): ListenerRegistration {
    val plugin = activePlugin
    val registrations = mutableListOf<RegisteredHandler>()
    val map = mutableMapOf<String, Pair<MethodListenerInfo, String?>>()
    collectEventMethods(listener.javaClass, map)

    for ((info, errors) in map.values) {
      if (errors != null) {
        val declaring = if (info.method != null) info.method.declaringClass.name else {
          (info.function.instanceParameter?.type?.classifier as? KClass<*>)?.qualifiedName
        } ?: "unknown"
        Terre.logger.warn("Invalid listener method ${info.function.name} in $declaring: $errors",
          info.function.name)
        continue
      }

      val untargetedHandler = methodHandlers
        .get(MethodInfo(info.method!!, info.function.isSuspend)) ?: error("Shouldn't happen.")
      val handler = object : EventHandler {
        override suspend fun handle(event: Event) {
          untargetedHandler.handle(listener, event)
        }
      }

      registrations.add(RegisteredHandler(plugin, info.order, info.eventType, listener, handler))
    }

    register(registrations)
    return object : ListenerRegistration {
      override fun unregister() {
        unregister(listener)
      }
    }
  }

  override fun unregister(listener: Any) {
    unregisterIf { it.instance === listener }
  }

  private fun unregisterIf(fn: (RegisteredHandler) -> Boolean) {
    val removed = mutableListOf<RegisteredHandler>()
    lock.write {
      val it = handlersByEvent.values().iterator()
      while (it.hasNext()) {
        val handler = it.next()
        if (fn(handler)) {
          it.remove()
          removed += handler
        }
      }
    }

    // Invalidate the affected event types
    handlersCache.invalidateAll(removed.flatMap { it.eventType.eventTypes }.distinct())
  }

  private class MethodListenerInfo(
      val function: KFunction<*>,
      val method: Method?,
      val eventType: Class<*>,
      val order: Int
  )

  private fun collectEventMethods(type: Class<*>, map: MutableMap<String, Pair<MethodListenerInfo, String?>>) {
    // We can't depend on methods directly, we must use kotlin functions,
    // see: https://youtrack.jetbrains.com/issue/KT-34024
    for (function in type.kotlin.declaredFunctions) {
      val annotation = function.findAnnotation<Subscribe>() ?: continue
      val errors = mutableSetOf<String>()
      val javaMethod = function.javaMethod
      if (!function.isSuspend && javaMethod?.isSynthetic == true)
        continue
      if (javaMethod != null && Modifier.isStatic(javaMethod.modifiers))
        errors += "function must not be static"
      val suspend = if (function.isSuspend) "suspend " else ""
      val key = suspend + function.name +
        function.parameters.joinToString(separator = ",", prefix = "(", postfix = ")")
      if (key in map)
        continue
      var eventType: Class<*> = Event::class.java
      if (function.isOperator)
        errors += "function must not be an operator"
      if (function.isInline)
        errors += "function must not be inlined"
      if (function.parameters.size != 2) {
        errors += "function must have 1 parameter which is the event"
      } else {
        val parameter = function.parameters[1]
        eventType = (parameter.type.classifier as KClass<*>).java
        if (!Event::class.java.isAssignableFrom(eventType))
          errors += "parameter isn't an event"
        if (parameter.isOptional)
          errors += "parameter can't be optional"
        if (parameter.isVararg)
          errors += "parameter can't be vararg"
      }
      val info = MethodListenerInfo(function, javaMethod, eventType, annotation.order)
      map[key] = info to if (errors.isEmpty()) null else errors.joinToString(", ")
    }
    for (itf in type.interfaces)
      collectEventMethods(itf, map)
    if (type.superclass != Any::class.java)
      collectEventMethods(type.superclass, map)
  }

  private val untargetedEventHandlerType = lambdaType<UntargetedEventHandler>()
  private val untargetedEventHandlerNoSuspendType = lambdaType<UntargetedEventHandler.NoSuspend>()

  private val methodHandlesLookup = MethodHandles.lookup()

  private fun buildMethodListener(methodInfo: MethodInfo): UntargetedEventHandler {
    val methodHandle = methodHandlesLookup
      .privateLookupIn(methodInfo.method.declaringClass).unreflect(methodInfo.method)

    val handlerType =
      if (methodInfo.isSuspend) untargetedEventHandlerType
      else untargetedEventHandlerNoSuspendType
    return methodHandle.createLambda(handlerType)
  }

  private fun register(listeners: List<RegisteredHandler>) {
    lock.write {
      for (listener in listeners) {
        handlersByEvent.put(listener.eventType, listener)
      }
    }
    // Invalidate all the affected event subtypes
    handlersCache.invalidateAll(listeners.flatMap { it.eventType.eventTypes }.distinct())
  }

  override suspend fun <T : Event> post(event: T) {
    val handlers = getHandlers(event.javaClass)
    if (handlers.isEmpty())
      return
    handleHandlers(event, handlers)
  }

  override fun <T : Event> postAndForget(event: T) {
    post(event) {}
  }

  override fun <T : Event> postAsync(event: T): Deferred<T> {
    val deferred = CompletableDeferred<T>()
    post(event) { deferred.complete(it); }
    return deferred
  }

  fun <T : Event> postAsyncWithFuture(event: T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    post(event) { future.complete(it); }
    return future
  }

  private inline fun <T : Event> post(event: T, crossinline complete: (T) -> Unit) {
    val listeners = getHandlers(event.javaClass)
    // Only launch a coroutine if necessary
    if (listeners.isEmpty()) {
      complete(event)
      return
    }
    coroutineScope.launch {
      handleHandlers(event, listeners)
      complete(event)
    }
  }

  private suspend fun handleHandlers(event: Event, handlers: List<RegisteredHandler>) {
    for (handler in handlers) {
      val plugin = handler.plugin
      try {
        withContext(PluginThreadLocalElement(plugin)) {
          handler.handler.handle(event)
        }
      } catch (ex: Throwable) {
        Terre.logger.info("Couldn't pass ${event::class.simpleName}" +
          if (plugin == null) "" else " to ${plugin.id}", ex)
      }
    }
  }
}
