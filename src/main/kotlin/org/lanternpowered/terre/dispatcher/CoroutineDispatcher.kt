/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("DeferredIsResult")

package org.lanternpowered.terre.dispatcher

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.lanternpowered.terre.Proxy
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Dispatches execution of a runnable [block] onto another thread in the given [context].
 *
 * This method should be generally exception-safe, an exception thrown from this method
 * may leave the coroutines that use this dispatcher in the inconsistent and hard to debug state.
 */
inline fun CoroutineDispatcher.dispatch(
    context: CoroutineContext = EmptyCoroutineContext, crossinline block: () -> Unit) {
  this.dispatch(context, Runnable { block() })
}

/**
 * Submits a task on the [CoroutineDispatcher].
 */
fun <T> CoroutineDispatcher.submit(
    context: CoroutineContext = EmptyCoroutineContext, block: () -> T): Deferred<T> {
  val deferred = CompletableDeferred<T>()
  dispatch(context) {
    try {
      deferred.complete(block())
    } catch (throwable: Throwable) {
      deferred.completeExceptionally(throwable)
    }
  }
  return deferred
}

/**
 * Launches a new async task on the proxy coroutine dispatcher.
 */
fun <T, R> T.letAsync(block: suspend (T) -> R): Deferred<R> {
  val scope = CoroutineScope(Proxy.dispatcher)
  return scope.async {
    block(this@letAsync)
  }
}

/**
 * Launches a new async task on the proxy coroutine dispatcher.
 */
fun <T, R> T.runAsync(block: suspend T.() -> R): Deferred<R> {
  val scope = CoroutineScope(Proxy.dispatcher)
  return scope.async {
    block()
  }
}

fun <T> T.applyAsync(block: suspend T.() -> Unit): T {
  val scope = CoroutineScope(Proxy.dispatcher)
  scope.launch {
    block()
  }
  return this
}

fun <T> T.alsoAsync(block: suspend (T) -> Unit): T {
  val scope = CoroutineScope(Proxy.dispatcher)
  scope.launch {
    block(this@alsoAsync)
  }
  return this
}

fun <T, R> withAsync(receiver: T, block: T.() -> R): Deferred<R> {
  val scope = CoroutineScope(Proxy.dispatcher)
  return scope.async {
    block(receiver)
  }
}
