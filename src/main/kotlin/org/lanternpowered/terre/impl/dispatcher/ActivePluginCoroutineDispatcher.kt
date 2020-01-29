/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import org.lanternpowered.terre.impl.plugin.ActivePluginThreadLocalElement
import org.lanternpowered.terre.impl.plugin.ActivePluginThreadLocalKey
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineDispatcher] that makes sure that the
 * [ActivePluginThreadLocalElement] is attached to
 * the context.
 */
internal class ActivePluginCoroutineDispatcher(
    private val backing: CoroutineDispatcher
) : CoroutineDispatcher() {

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    var dispatchContext = context
    if (context[ActivePluginThreadLocalKey] == null) {
      dispatchContext += ActivePluginThreadLocalElement()
    }
    this.backing.dispatch(dispatchContext, block)
  }

  @ExperimentalCoroutinesApi
  override fun isDispatchNeeded(context: CoroutineContext) = this.backing.isDispatchNeeded(context)

  @InternalCoroutinesApi
  override fun dispatchYield(context: CoroutineContext, block: Runnable) {
    this.backing.dispatchYield(context, block)
  }

  override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
    this.backing.releaseInterceptedContinuation(continuation)
  }

  override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? {
    return super.get(key) ?: this.backing[key]
  }
}
