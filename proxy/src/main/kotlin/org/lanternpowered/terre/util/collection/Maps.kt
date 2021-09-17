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

import com.google.common.collect.ImmutableMap

inline fun <K, V> Map<K, V>.toImmutableMap(): ImmutableMap<K, V> =
  ImmutableMap.copyOf(this)
