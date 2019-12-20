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
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap

inline fun <K : Any, V : Any> Map<K, V>.toImmutableMap(): ImmutableMap<K, V>
    = ImmutableMap.copyOf(this)

inline fun <K : Any, V : Any> Multimap<K, V>.toImmutableMultimap(): ImmutableMultimap<K, V>
    = ImmutableMultimap.copyOf(this)

inline fun <K, V> multimapOf(): Multimap<K, V>
    = ImmutableMultimap.of()

inline fun <K, V> mutableMultimapOf(): Multimap<K, V>
    = HashMultimap.create()
