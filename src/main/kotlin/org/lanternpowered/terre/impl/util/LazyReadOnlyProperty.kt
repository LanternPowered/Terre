/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNCHECKED_CAST")

package org.lanternpowered.terre.impl.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@PublishedApi
internal class LazyReadOnlyProperty<R, T>(initializer: R.() -> T) : ReadOnlyProperty<R, T> {

  private object Uninitialized

  private val lock = Any()
  private var initializer: (R.() -> T)? = initializer
  @Volatile private var value: Any? = Uninitialized

  override fun getValue(thisRef: R, property: KProperty<*>): T {
    var cached = this.value
    if (cached !== Uninitialized) {
      return cached as T
    }
    synchronized(this.lock) {
      cached = this.value
      if (cached !== Uninitialized) {
        return cached as T
      }
      cached = this.initializer!!(thisRef)
      this.initializer = null
      this.value = cached
      return cached as T
    }
  }
}
