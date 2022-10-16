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
import org.lanternpowered.terre.util.collection.toImmutableCollection
import org.lanternpowered.terre.util.collection.toImmutableMap
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

internal fun <T : NumericCatalogType> buildNumericCatalogTypeRegistryOf(
  type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): NumericCatalogTypeRegistryImpl<T> {
  val builder = NumericCatalogTypeRegistryBuilderImpl<T>()
  fn(builder)
  return NumericCatalogTypeRegistryImpl(type, builder.byNumericId.toImmutableMap())
}

internal inline fun <reified T : NumericCatalogType> buildNumericCatalogTypeRegistryOf(
  noinline autoRegisterFunction: (Int) -> T?,
  noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit = {}
) = buildNumericCatalogTypeRegistryOf(T::class, autoRegisterFunction, fn)

internal fun <T : NumericCatalogType> buildNumericCatalogTypeRegistryOf(
  type: KClass<T>, autoRegisterFunction: (Int) -> T?,
  builder: CatalogTypeRegistryBuilder<T>.() -> Unit = {}
): NumericCatalogTypeRegistryImpl<T> {
  val builderImpl = NumericCatalogTypeRegistryBuilderImpl<T>()
  builder(builderImpl)
  return NumericCatalogTypeRegistryImpl(type,
    ConcurrentHashMap(builderImpl.byNumericId), autoRegisterFunction)
}

internal open class NumericCatalogTypeRegistryBuilderImpl<T : NumericCatalogType> :
  CatalogTypeRegistryBuilder<T> {

  val byNumericId = hashMapOf<Int, T>()

  override fun register(catalogType: T) {
    check(!byNumericId.containsKey(catalogType.numericId)) {
      "The numeric id '${catalogType.numericId}' is already in use." }
    byNumericId[catalogType.numericId] = catalogType
  }
}

internal class NumericCatalogTypeRegistryImpl<T : NumericCatalogType>(
  private val type: KClass<T>,
  private val byNumericId: MutableMap<Int, T>,
  private val autoRegisterFunction: ((Int) -> T?)? = null
) : NumericCatalogTypeRegistry<T> {

  override val all: Collection<T>
    get() = byNumericId.values.toImmutableCollection()

  override fun get(numericId: Int): T? {
    var catalogType = byNumericId[numericId]
    val autoRegisterFunction = autoRegisterFunction
    if (catalogType != null || autoRegisterFunction == null)
      return catalogType
    catalogType = autoRegisterFunction(numericId) ?: return null
    byNumericId[numericId] = catalogType
    return catalogType
  }

  override fun require(numericId: Int): T =
    this[numericId] ?: throw IllegalArgumentException(
      "${type.simpleName} with numeric id '$numericId' doesn't exist.")
}
