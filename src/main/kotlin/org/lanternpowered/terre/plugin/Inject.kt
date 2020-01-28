/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.plugin

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Injects a value of type [T] for
 * the target receiver.
 */
inline fun <reified T> Any.inject(): T {
  @Suppress("UNCHECKED_CAST")
  return inject0(typeOf<T>()) as T
}

/**
 * Injects a value of type [T] for
 * the target receiver.
 */
fun <T> Any.inject(type: KType): T {
  @Suppress("UNCHECKED_CAST")
  return inject0(type) as T
}

/**
 * Injects a value of type [T] within the global scope.
 */
inline fun <reified T : Any> inject(): T {
  @Suppress("UNCHECKED_CAST")
  return null.inject0(typeOf<T>()) as T
}

/**
 * Injects a value of type [T] within the global scope.
 */
fun <T> inject(type: KType): T {
  @Suppress("UNCHECKED_CAST")
  return null.inject0(type) as T
}

@PublishedApi
internal fun Any?.inject0(type: KType): Any? {
  TODO()
}
