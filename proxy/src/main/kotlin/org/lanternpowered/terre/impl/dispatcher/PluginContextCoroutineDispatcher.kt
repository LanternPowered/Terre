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

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import org.lanternpowered.terre.impl.plugin.PluginThreadLocalElement
import org.lanternpowered.terre.plugin.PluginContextElement
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineDispatcher] that makes sure that the [PluginThreadLocalElement] is attached to the
 * context.
 */
@InternalCoroutinesApi
internal class PluginContextCoroutineDispatcher(
  private val backing: CoroutineDispatcher
) : CoroutineDispatcher(), Delay {

  private val delay = backing as Delay

  private fun populateContext(context: CoroutineContext): CoroutineContext {
    return if (context[PluginContextElement.Key] == null) {
      context + PluginContextElement()
    } else context
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    backing.dispatch(populateContext(context), block)
  }

  override fun isDispatchNeeded(context: CoroutineContext) = backing.isDispatchNeeded(context)

  @InternalCoroutinesApi
  override fun dispatchYield(context: CoroutineContext, block: Runnable) {
    backing.dispatchYield(populateContext(context), block)
  }

  override fun scheduleResumeAfterDelay(
    timeMillis: Long, continuation: CancellableContinuation<Unit>
  ) {
    delay.scheduleResumeAfterDelay(timeMillis, continuation)
  }

  override fun invokeOnTimeout(
    timeMillis: Long, block: Runnable, context: CoroutineContext
  ): DisposableHandle =
    delay.invokeOnTimeout(timeMillis, block, context)
}
