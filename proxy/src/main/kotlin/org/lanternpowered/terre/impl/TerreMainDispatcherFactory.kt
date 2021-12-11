/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.internal.MainDispatcherFactory
import org.lanternpowered.terre.impl.event.EventExecutor
import kotlin.coroutines.CoroutineContext

@InternalCoroutinesApi
internal class TerreMainDispatcherFactory : MainDispatcherFactory {

  override val loadPriority: Int = Int.MAX_VALUE // Highest priority, we need to win this!

  override fun createDispatcher(
    allFactories: List<MainDispatcherFactory>
  ): MainCoroutineDispatcher =
    TerreMainCoroutineDispatcher(EventExecutor.pluginAwareDispatcher)
}

@InternalCoroutinesApi
private class TerreMainCoroutineDispatcher(
  private val dispatcher: CoroutineDispatcher,
  private val invokeImmediately: Boolean = false
) : MainCoroutineDispatcher(), Delay {

  private val delay = dispatcher as Delay

  override val immediate: MainCoroutineDispatcher =
    if (invokeImmediately) this else TerreMainCoroutineDispatcher(dispatcher, true)

  override fun isDispatchNeeded(context: CoroutineContext): Boolean =
    !invokeImmediately || dispatcher.isDispatchNeeded(context)

  override fun dispatchYield(context: CoroutineContext, block: Runnable) =
    dispatcher.dispatchYield(context, block)

  override fun dispatch(context: CoroutineContext, block: Runnable) =
    dispatcher.dispatch(context, block)

  override fun scheduleResumeAfterDelay(
    timeMillis: Long, continuation: CancellableContinuation<Unit>
  ) = delay.scheduleResumeAfterDelay(timeMillis, continuation)

  override fun invokeOnTimeout(
    timeMillis: Long, block: Runnable, context: CoroutineContext
  ): DisposableHandle =
    delay.invokeOnTimeout(timeMillis, block, context)
}
