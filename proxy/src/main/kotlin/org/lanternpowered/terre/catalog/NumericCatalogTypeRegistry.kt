/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.catalog

import org.lanternpowered.terre.impl.buildNumericCatalogTypeRegistryOf
import kotlin.reflect.KClass

/**
 * Constructs a new [CatalogTypeRegistry].
 */
inline fun <reified T : NumericCatalogType> numericCatalogTypeRegistry(
  noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): NumericCatalogTypeRegistry<T> = numericCatalogTypeRegistry(T::class, fn)

/**
 * Constructs a new [CatalogTypeRegistry].
 */
fun <T : NumericCatalogType> numericCatalogTypeRegistry(
  type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): NumericCatalogTypeRegistry<T> = buildNumericCatalogTypeRegistryOf(type, fn)

interface NumericCatalogTypeRegistry<T : NumericCatalogType> : CatalogTypeRegistry<T> {

  /**
   * Gets a catalog type of type [T] from the registry, if present.
   */
  operator fun get(numericId: Int): T?

  /**
   * Requires that a catalog type of the given numeric id exists and returns it.
   */
  fun require(numericId: Int): T
}
