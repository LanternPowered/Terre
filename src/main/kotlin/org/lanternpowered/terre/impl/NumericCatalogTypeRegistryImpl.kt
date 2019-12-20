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

import org.lanternpowered.terre.catalog.CatalogTypeRegistryBuilder
import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.catalog.NumericCatalogTypeRegistry
import org.lanternpowered.terre.catalog.NumericId
import org.lanternpowered.terre.util.NamespacedId
import org.lanternpowered.terre.util.collection.toImmutableMap
import kotlin.reflect.KClass

internal fun <T : NumericCatalogType> buildImmutableNumericCatalogTypeRegistryOf(
    type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): ImmutableNumericCatalogTypeRegistryImpl<T> {
  val builder = InternalCatalogTypeRegistryBuilderImpl<T>()
  fn(builder)
  return ImmutableNumericCatalogTypeRegistryImpl(type, builder.byId.toImmutableMap())
}

internal fun <T : NumericCatalogType> buildMutableNumericCatalogTypeRegistryOf(
    type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): MutableNumericCatalogTypeRegistryImpl<T> {
  val registry = MutableNumericCatalogTypeRegistryImpl(type)
  val builder = MutableCatalogTypeRegistryBuilderImpl(registry)
  fn(builder)
  return registry
}

internal class MutableNumericCatalogTypeRegistryImpl<T : NumericCatalogType>(
    override val type: KClass<T>
) : MutableInternalCatalogTypeRegistryImpl<T>(type), NumericCatalogTypeRegistry<T> {

  override fun get(numericId: NumericId): T? = get(numericId.value)
}

internal class ImmutableNumericCatalogTypeRegistryImpl<T : NumericCatalogType>(
    override val type: KClass<T>, byId: Map<NamespacedId, T>
) : ImmutableInternalCatalogTypeRegistryImpl<T>(type, byId), NumericCatalogTypeRegistry<T> {

  override fun get(numericId: NumericId): T? = get(numericId.value)
}
