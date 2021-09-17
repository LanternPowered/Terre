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

import org.lanternpowered.terre.impl.buildNamedCatalogTypeRegistryOf
import kotlin.reflect.KClass

/**
 * Constructs a new [CatalogTypeRegistry].
 */
inline fun <reified T : NamedCatalogType> namedCatalogTypeRegistry(
  noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): NamedCatalogTypeRegistry<T> = namedCatalogTypeRegistry(T::class, fn)

/**
 * Constructs a new [CatalogTypeRegistry].
 */
fun <T : NamedCatalogType> namedCatalogTypeRegistry(
  type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): NamedCatalogTypeRegistry<T> = buildNamedCatalogTypeRegistryOf(type, fn)

/**
 * Represents a registry for named catalog types.
 */
interface NamedCatalogTypeRegistry<T : NamedCatalogType> : CatalogTypeRegistry<T> {

  /**
   * Gets a catalog type of type [T] from
   * the registry, if present.
   */
  operator fun get(name: String): T?

  /**
   * Requires that a catalog type of the
   * given name exists and returns it.
   */
  fun require(name: String): T
}
