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

import org.lanternpowered.terre.impl.buildImmutableCatalogTypeRegistryOf
import org.lanternpowered.terre.impl.buildMutableCatalogTypeRegistryOf
import org.lanternpowered.terre.util.NamespacedId
import kotlin.reflect.KClass

/**
 * Constructs a new [CatalogTypeRegistry].
 */
inline fun <reified T : CatalogType> catalogTypeRegistry(noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit):
    CatalogTypeRegistry<T> = catalogTypeRegistry(T::class, fn)

/**
 * Constructs a new [CatalogTypeRegistry].
 */
fun <T : CatalogType> catalogTypeRegistry(type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit):
    CatalogTypeRegistry<T> = buildImmutableCatalogTypeRegistryOf(type, fn)

/**
 * Constructs a new [MutableCatalogTypeRegistry].
 */
inline fun <reified T : CatalogType> mutableCatalogTypeRegistry(noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit = {}):
    MutableCatalogTypeRegistry<T> = mutableCatalogTypeRegistry(T::class, fn)

/**
 * Constructs a new [MutableCatalogTypeRegistry].
 */
fun <T : CatalogType> mutableCatalogTypeRegistry(type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit = {}):
    MutableCatalogTypeRegistry<T> = buildMutableCatalogTypeRegistryOf(type, fn)

/**
 * Represents a registry for catalog types.
 */
interface CatalogTypeRegistry<T : CatalogType> {

  /**
   * A collection with all the registered types.
   */
  val all: Collection<T>

  /**
   * Gets a catalog type of type [T] from
   * the registry, if present.
   */
  operator fun get(id: NamespacedId): T?

  /**
   * Requires a catalog type of type [T] from
   * the registry.
   */
  fun require(id: NamespacedId): T
}

/**
 * A registry that allows registrations after it's initial creation.
 */
interface MutableCatalogTypeRegistry<T : CatalogType> : CatalogTypeRegistry<T> {

  /**
   * Registers a catalog type.
   */
  fun register(catalogType: T)
}

/**
 * Represents a builder for catalog registries.
 */
interface CatalogTypeRegistryBuilder<T : CatalogType> {

  /**
   * Registers a catalog type.
   */
  fun register(catalogType: T)
}
