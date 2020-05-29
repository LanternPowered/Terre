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
import org.lanternpowered.terre.impl.plugin.PluginThreadLocalElement
import org.lanternpowered.terre.plugin.PluginContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineDispatcher] that makes sure that the
 * [PluginThreadLocalElement] is attached to
 * the context.
 */
internal class PluginContextCoroutineDispatcher(
    private val backing: CoroutineDispatcher
) : CoroutineDispatcher() {

  private fun populateContext(context: CoroutineContext): CoroutineContext {
    return if (context[PluginContextElement.Key] == null) context + PluginContextElement() else context
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    this.backing.dispatch(populateContext(context), block)
  }

  @ExperimentalCoroutinesApi
  override fun isDispatchNeeded(context: CoroutineContext) = this.backing.isDispatchNeeded(context)

  @InternalCoroutinesApi
  override fun dispatchYield(context: CoroutineContext, block: Runnable) {
    this.backing.dispatchYield(populateContext(context), block)
  }

  @InternalCoroutinesApi
  override fun releaseInterceptedContinuation(continuation: Continuation<*>) {
    this.backing.releaseInterceptedContinuation(continuation)
  }
}
