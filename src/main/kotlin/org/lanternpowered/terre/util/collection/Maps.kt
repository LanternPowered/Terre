/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NOTHING_TO_INLINE")

package org.lanternpowered.terre.util.collection

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap

inline fun <K, V> Map<K, V>.toImmutableMap(): ImmutableMap<K, V>
    = ImmutableMap.copyOf(this)

fun <K, V> Multimap<K, V>.toImmutableMultimap(): ImmutableMultimap<K, V>
    = this as? ImmutableMultimap<K, V> ?: ImmutableListMultimap.copyOf(this)

@JvmName("toMultimapFromSequence")
fun <K, V> Map<K, Sequence<V>>.toMultimap(): Multimap<K, V> {
  return toMultimap { it.iterator() }
}

fun <K, V> Map<K, Iterable<V>>.toMultimap(): Multimap<K, V> {
  return toMultimap { it.iterator() }
}

private inline fun <K, V, I : Any> Map<K, I>.toMultimap(
    fn: (I) -> Iterator<V>
): Multimap<K, V> {
  val map = HashMultimap.create<K, V>()
  for ((key, iterable) in this) {
    fn(iterable).forEach { value ->
      map.put(key, value)
    }
  }
  return map
}

@JvmName("toImmutableMultimapFromSequence")
fun <K, V> Map<K, Sequence<V>>.toImmutableMultimap(): ImmutableMultimap<K, V> {
  return toImmutableMultimap { it.iterator() }
}

fun <K, V> Map<K, Iterable<V>>.toImmutableMultimap(): ImmutableMultimap<K, V> {
  return toImmutableMultimap { it.iterator() }
}

private inline fun <K, V, I : Any> Map<K, I>.toImmutableMultimap(
    fn: (I) -> Iterator<V>
): ImmutableMultimap<K, V> {
  if (this.isEmpty()) {
    return ImmutableMultimap.of()
  }
  val builder = ImmutableMultimap.builder<K, V>()
  for ((key, iterable) in this) {
    fn(iterable).forEach { value ->
      builder.put(key, value)
    }
  }
  return builder.build()
}

inline fun <K, V> multimapOf(): Multimap<K, V>
    = ImmutableMultimap.of()

inline fun <K, V> multimapOf(pair: Pair<K, V>): Multimap<K, V>
    = ImmutableMultimap.of(pair.first, pair.second)

fun <K, V> multimapOf(vararg pairs: Pair<K, V>): Multimap<K, V> {
  if (pairs.isEmpty()) {
    return ImmutableMultimap.of()
  }
  val builder = ImmutableMultimap.builder<K, V>()
  for (pair in pairs) {
    builder.put(pair.first, pair.second)
  }
  return builder.build()
}

inline fun <K, V> mutableMultimapOf(): Multimap<K, V>
    = HashMultimap.create()
