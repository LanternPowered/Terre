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

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener

@Suppress("RedundantSamConstructor")
internal inline fun ChannelFuture.addChannelFutureListener(
  crossinline block: (future: ChannelFuture) -> Unit
): ChannelFuture {
  return this.addListener(ChannelFutureListener {
    block(it)
  })
}
