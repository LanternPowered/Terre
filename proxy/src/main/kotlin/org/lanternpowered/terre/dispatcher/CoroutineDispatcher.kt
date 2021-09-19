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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.lanternpowered.terre.impl.event.EventExecutor
import org.lanternpowered.terre.plugin.PluginContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Dispatches execution of a runnable [block] onto another thread in the given [context].
 *
 * This method should be generally exception-safe, an exception thrown from this method may leave
 * the coroutines that use this dispatcher in the inconsistent and hard to debug state.
 */
inline fun CoroutineDispatcher.dispatch(
  context: CoroutineContext = EmptyCoroutineContext, crossinline block: () -> Unit
) {
  dispatch(context) { block() }
}

/**
 * Submits a task on the [CoroutineDispatcher].
 */
fun <T> CoroutineDispatcher.submit(
  context: CoroutineContext = EmptyCoroutineContext, block: () -> T
): Deferred<T> {
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
 * Creates a new coroutine scope.
 */
private fun newCoroutineScope() =
  CoroutineScope(EventExecutor.dispatcher + PluginContextElement())

/**
 * Launches a new async job on the proxy coroutine dispatcher.
 */
fun launchAsync(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend CoroutineScope.() -> Unit
): Job {
  return newCoroutineScope().launch(context) {
    block()
  }
}

fun <T, R> T.letAsync(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend (T) -> R
): Deferred<R> {
  return newCoroutineScope().async(context) {
    block(this@letAsync)
  }
}

fun <T, R> T.runAsync(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend T.() -> R
): Deferred<R> {
  return newCoroutineScope().async(context) {
    block()
  }
}

fun <T> T.applyAsync(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend T.() -> Unit
): Deferred<T> {
  return newCoroutineScope().async(context) {
    block()
    this@applyAsync
  }
}

fun <T> T.alsoAsync(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend (T) -> Unit
): Deferred<T> {
  return newCoroutineScope().async(context) {
    block(this@alsoAsync)
    this@alsoAsync
  }
}

fun <T, R> withAsync(
  context: CoroutineContext = EmptyCoroutineContext,
  receiver: T,
  block: suspend T.() -> R
): Deferred<R> {
  return newCoroutineScope().async(context) {
    block(receiver)
  }
}
