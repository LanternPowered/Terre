/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import org.lanternpowered.terre.impl.util.TerreThread
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

internal class NettyThreadFactory(private val group: String) : ThreadFactory {

  private val counter = AtomicInteger()

  override fun newThread(r: Runnable): Thread =
    TerreThread(r, "netty-$group-${counter.incrementAndGet()}")
}
