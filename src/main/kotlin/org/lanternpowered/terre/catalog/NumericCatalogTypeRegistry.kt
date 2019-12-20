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

import org.lanternpowered.terre.impl.buildImmutableNumericCatalogTypeRegistryOf
import org.lanternpowered.terre.impl.buildMutableNumericCatalogTypeRegistryOf
import kotlin.reflect.KClass

/**
 * Constructs a new [CatalogTypeRegistry].
 */
inline fun <reified T : NumericCatalogType> numericCatalogTypeRegistry(noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit):
    NumericCatalogTypeRegistry<T> = numericCatalogTypeRegistry(T::class, fn)

/**
 * Constructs a new [CatalogTypeRegistry].
 */
fun <T : NumericCatalogType> numericCatalogTypeRegistry(type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit):
    NumericCatalogTypeRegistry<T> = buildImmutableNumericCatalogTypeRegistryOf(type, fn)

/**
 * Constructs a new [CatalogTypeRegistry].
 */
inline fun <reified T : NumericCatalogType> mutableCatalogTypeRegistry(noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit = {}):
    NumericCatalogTypeRegistry<T> = mutableCatalogTypeRegistry(T::class, fn)

/**
 * Constructs a new [CatalogTypeRegistry].
 */
fun <T : NumericCatalogType> mutableCatalogTypeRegistry(type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit = {}):
    NumericCatalogTypeRegistry<T> = buildMutableNumericCatalogTypeRegistryOf(type, fn)

interface NumericCatalogTypeRegistry<T : NumericCatalogType> : CatalogTypeRegistry<T> {

  /**
   * Gets a catalog type of type [T] from
   * the registry, if present.
   */
  operator fun get(numericId: NumericId): T?
}
