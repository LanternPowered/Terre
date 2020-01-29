/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.plugin

import io.netty.util.concurrent.FastThreadLocal
import kotlinx.coroutines.ThreadContextElement
import org.lanternpowered.terre.impl.util.TerreThread
import org.lanternpowered.terre.plugin.PluginContainer
import kotlin.coroutines.CoroutineContext

/**
 * A fallback thread local in case the
 * thread isn't a [TerreThread].
 */
private val threadLocal = FastThreadLocal<PluginContainer>()

/**
 * The current active plugin of the thread.
 */
@PublishedApi
internal var Thread.activePlugin: PluginContainer?
  get() = if (this is TerreThread) {
    this.activePlugin
  } else {
    threadLocal.get()
  }
  set(value) = when {
    this is TerreThread -> this.activePlugin = value
    value != null -> threadLocal.set(value)
    else -> threadLocal.remove()
  }

internal object ActivePluginThreadLocalKey : CoroutineContext.Key<ActivePluginThreadLocalElement>

internal class ActivePluginThreadLocalElement(
    private val value: PluginContainer? = Thread.currentThread().activePlugin
) : ThreadContextElement<PluginContainer?> {

  override val key: CoroutineContext.Key<*> get() = ActivePluginThreadLocalKey

  override fun updateThreadContext(context: CoroutineContext): PluginContainer? {
    val thread = Thread.currentThread()
    val oldState = thread.activePlugin
    thread.activePlugin = this.value
    return oldState
  }

  override fun restoreThreadContext(context: CoroutineContext, oldState: PluginContainer?) {
    Thread.currentThread().activePlugin = oldState
  }

  override fun toString(): String = "ActivePlugin(plugin=$value)"
}
