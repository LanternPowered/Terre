/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util.coroutines

import io.netty.util.concurrent.FastThreadLocal
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Wraps [FastThreadLocal] into [ThreadContextElement].
 *
 * [kotlinx.coroutines.asContextElement] but for [FastThreadLocal].
 */
internal fun <T> FastThreadLocal<T>.asContextElement(value: T = get()): ThreadContextElement<T>
    = FastThreadLocalElement(value, this)

private data class FastThreadLocalKey(
    private val threadLocal: FastThreadLocal<*>
) : CoroutineContext.Key<FastThreadLocalElement<*>>

private class FastThreadLocalElement<T>(
    private val value: T,
    private val threadLocal: FastThreadLocal<T>
) : ThreadContextElement<T> {

  override val key = FastThreadLocalKey(this.threadLocal)

  override fun updateThreadContext(context: CoroutineContext): T {
    val oldState = this.threadLocal.get()
    this.threadLocal.set(this.value)
    return oldState
  }

  override fun restoreThreadContext(context: CoroutineContext, oldState: T) {
    this.threadLocal.set(oldState)
  }

  override fun toString(): String = "FastThreadLocal(value=$value, threadLocal=$threadLocal)"
}
