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

/**
 * Represents a registry for catalog types.
 */
interface CatalogTypeRegistry<T : CatalogType> {

  /**
   * A collection with all the registered types.
   */
  val all: Collection<T>
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
