/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util.channel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ChannelResult
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * A channel that only allows the same value to be queued once at the same time.
 */
internal fun <E> Channel<E>.distinct(): Channel<E> = DistinctChannel(this) { it }

/**
 * A channel that only allows the same key produced by [keyProvider] to be queued once at the
 * same time.
 */
internal fun <E, K> Channel<E>.distinctBy(keyProvider: (E) -> K): Channel<E> =
  DistinctChannel(this, keyProvider)

private class DistinctChannel<E, K>(val channel: Channel<E>, val keyProvider: (E) -> K) :
  Channel<E> by channel {

  private val keys = Collections.newSetFromMap(ConcurrentHashMap<K, Boolean>())

  override fun trySend(element: E): ChannelResult<Unit> {
    if (!keys.add(keyProvider(element)))
      return ChannelResult.failure()
    return channel.trySend(element)
  }

  override suspend fun send(element: E) {
    if (keys.add(keyProvider(element)))
      channel.send(element)
  }

  override fun iterator(): ChannelIterator<E> {
    val it = channel.iterator()
    return object : ChannelIterator<E> {
      override suspend fun hasNext() = it.hasNext()
      override fun next(): E {
        val value = it.next()
        keys.remove(keyProvider(value))
        return value
      }
    }
  }

  override fun tryReceive(): ChannelResult<E> {
    val value = this.channel.tryReceive()
    @Suppress("UNCHECKED_CAST")
    if (value.isSuccess)
      keys.remove(keyProvider(value.getOrNull() as E))
    return value
  }

  override suspend fun receive(): E {
    val value = channel.receive()
    keys.remove(keyProvider(value))
    return value
  }

  override suspend fun receiveCatching(): ChannelResult<E> {
    val value = channel.receiveCatching()
    @Suppress("UNCHECKED_CAST")
    if (value.isSuccess)
      keys.remove(keyProvider(value.getOrNull() as E))
    return value
  }
}
