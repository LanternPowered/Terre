/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import org.lanternpowered.terre.catalog.CatalogType
import org.lanternpowered.terre.catalog.CatalogTypeRegistry
import org.lanternpowered.terre.catalog.CatalogTypeRegistryBuilder
import org.lanternpowered.terre.catalog.MutableCatalogTypeRegistry
import org.lanternpowered.terre.util.NamespacedId
import org.lanternpowered.terre.util.collection.toImmutableList
import org.lanternpowered.terre.util.collection.toImmutableMap
import kotlin.reflect.KClass

internal fun <T : CatalogType> buildImmutableCatalogTypeRegistryOf(
    type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): ImmutableCatalogTypeRegistryImpl<T> {
  val builder = CatalogTypeRegistryBuilderImpl<T>()
  fn(builder)
  return ImmutableCatalogTypeRegistryImpl(type, builder.byId.toImmutableMap())
}

internal fun <T : CatalogType> buildMutableCatalogTypeRegistryOf(
    type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): MutableCatalogTypeRegistryImpl<T> {
  val registry = MutableCatalogTypeRegistryImpl(type)
  val builder = MutableCatalogTypeRegistryBuilderImpl(registry)
  fn(builder)
  return registry
}

internal open class CatalogTypeRegistryBuilderImpl<T : CatalogType> : CatalogTypeRegistryBuilder<T> {

  val byId = hashMapOf<NamespacedId, T>()

  override fun register(catalogType: T) {
    check(!this.byId.containsKey(catalogType.id)) { "The id '${catalogType.id}' is already in use." }
    this.byId[catalogType.id] = catalogType
  }
}

internal open class MutableCatalogTypeRegistryBuilderImpl<T : CatalogType>(
    private val registry: MutableCatalogTypeRegistry<T>
) : CatalogTypeRegistryBuilder<T> {

  override fun register(catalogType: T) {
    this.registry.register(catalogType)
  }
}

internal abstract class CatalogTypeRegistryImpl<T : CatalogType> : CatalogTypeRegistry<T> {

  protected abstract val type: KClass<T>
  protected abstract val byId: Map<NamespacedId, T>

  override val all: Collection<T>
    get() = this.byId.values.toImmutableList()

  override fun get(id: NamespacedId): T? = this.byId[id]

  override fun require(id: NamespacedId): T =
      this.byId[id] ?: throw IllegalArgumentException("${type.simpleName} with id '$id' doesn't exist.")
}

internal open class ImmutableCatalogTypeRegistryImpl<T : CatalogType>(
    override val type: KClass<T>,
    override val byId: Map<NamespacedId, T>
) : CatalogTypeRegistryImpl<T>()

internal open class MutableCatalogTypeRegistryImpl<T : CatalogType>(
    override val type: KClass<T>
) : CatalogTypeRegistryImpl<T>(), MutableCatalogTypeRegistry<T> {

  override val byId = hashMapOf<NamespacedId, T>()

  override fun register(catalogType: T) {
    check(!this.byId.containsKey(catalogType.id)) { "The id '${catalogType.id}' is already in use." }
    this.byId[catalogType.id] = catalogType
  }
}
