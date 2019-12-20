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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.lanternpowered.terre.catalog.CatalogType
import org.lanternpowered.terre.catalog.CatalogTypeRegistryBuilder
import org.lanternpowered.terre.catalog.MutableCatalogTypeRegistry
import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.util.NamespacedId
import org.lanternpowered.terre.util.collection.toImmutableMap
import kotlin.reflect.KClass

internal inline fun <reified T : CatalogType> internalCatalogTypeRegistryOf(
    noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): ImmutableInternalCatalogTypeRegistryImpl<T> {
  return internalCatalogTypeRegistryOf(T::class, fn)
}

internal fun <T : CatalogType> internalCatalogTypeRegistryOf(
    type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): ImmutableInternalCatalogTypeRegistryImpl<T> {
  val builder = InternalCatalogTypeRegistryBuilderImpl<T>()
  fn(builder)
  return ImmutableInternalCatalogTypeRegistryImpl(type, builder.byId.toImmutableMap())
}

internal class InternalCatalogTypeRegistryBuilderImpl<T : CatalogType> :
    CatalogTypeRegistryBuilderImpl<T>() {

  private val byInternalId = Int2ObjectOpenHashMap<T>()

  override fun register(catalogType: T) {
    catalogType as InternalCatalogType
    check(!this.byInternalId.containsKey(catalogType.internalId)) {
      "The numeric id '${catalogType.internalId}' is already in use." }
    super.register(catalogType)
    this.byInternalId[catalogType.internalId] = catalogType
  }
}

internal abstract class InternalCatalogTypeRegistryImpl<T : CatalogType> : CatalogTypeRegistryImpl<T>() {

  protected abstract val byNumericId: Int2ObjectMap<T>

  operator fun get(internalId: Int): T?
      = this.byNumericId[internalId]

  fun require(internalId: Int) = this.byNumericId[internalId] ?: throw IllegalArgumentException(
      "${type.simpleName} with internal id '$internalId' doesn't exist.")
}

internal open class ImmutableInternalCatalogTypeRegistryImpl<T : CatalogType>(
    override val type: KClass<T>,
    final override val byId: Map<NamespacedId, T>
) : InternalCatalogTypeRegistryImpl<T>() {

  final override val byNumericId = Int2ObjectOpenHashMap<T>()

  init {
    for ((_, catalogType) in this.byId) {
      catalogType as NumericCatalogType
      this.byNumericId[catalogType.numericId.value] = catalogType;
    }
  }
}

internal open class MutableInternalCatalogTypeRegistryImpl<T : CatalogType>(
    override val type: KClass<T>
) : InternalCatalogTypeRegistryImpl<T>(), MutableCatalogTypeRegistry<T> {

  override val byId = hashMapOf<NamespacedId, T>()
  override val byNumericId = Int2ObjectOpenHashMap<T>()

  override fun register(catalogType: T) {
    catalogType as InternalCatalogType
    check(!this.byId.containsKey(catalogType.id)) { "The id '${catalogType.id}' is already in use." }
    check(!this.byNumericId.containsKey(catalogType.internalId)) {
      "The numeric id '${catalogType.internalId}' is already in use." }
    this.byId[catalogType.id] = catalogType
    this.byNumericId[catalogType.internalId] = catalogType
  }
}
