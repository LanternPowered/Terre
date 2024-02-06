/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util

import io.netty.util.concurrent.FastThreadLocalThread
import org.lanternpowered.terre.plugin.PluginContainer

/**
 * Represents a custom thread to store thread local variables.
 *
 * Storing thread local values as fields instead of using
 * ThreadLocal objects has better performance.
 */
internal class TerreThread : FastThreadLocalThread {

  /**
   * The plugin that's currently active.
   */
  var activePlugin: PluginContainer? = null

  constructor(target: Runnable) : super(target)

  constructor(target: Runnable, name: String) : super(target, name)
}
