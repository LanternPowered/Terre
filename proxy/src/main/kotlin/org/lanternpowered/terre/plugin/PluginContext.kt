/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.lanternpowered.terre.impl.plugin.PluginThreadLocalElement
import org.lanternpowered.terre.impl.plugin.activePlugin
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

/**
 * Executes the block as if it was executed
 * by the given [PluginContainer].
 */
inline fun <R> withActivePlugin(pluginContainer: PluginContainer?, block: () -> R): R {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  val thread = Thread.currentThread()
  val old = thread.activePlugin
  thread.activePlugin = pluginContainer
  try {
    return block()
  } finally {
    thread.activePlugin = old
  }
}

/**
 * Executes the block as if it was executed
 * by the given [PluginContainer].
 */
suspend fun <R> CoroutineScope.withActivePlugin(
  pluginContainer: PluginContainer?, block: suspend CoroutineScope.() -> R
): R {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  return withContext(pluginContainer.asContextElement()) {
    block(this@withActivePlugin)
  }
}

/**
 * Constructs a new [PluginContextElement] from the target [PluginContainer].
 */
fun PluginContainer?.asContextElement() = PluginContextElement(this)

/**
 * Constructs a new [PluginContextElement].
 */
fun PluginContextElement(
  pluginContainer: PluginContainer? = PluginContainer.Active
): PluginContextElement {
  return PluginThreadLocalElement(pluginContainer)
}

/**
 * A context element of a coroutine which represents
 * the "active plugin".
 */
interface PluginContextElement : CoroutineContext.Element {

  /**
   * Represents the key of the [PluginContextElement].
   */
  companion object Key : CoroutineContext.Key<PluginContextElement>

  /**
   * The [PluginContainer] of the context element, if any.
   */
  val plugin: PluginContainer?

  override val key
    get() = Key
}
