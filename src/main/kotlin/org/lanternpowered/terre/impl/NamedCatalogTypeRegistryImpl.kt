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
import org.lanternpowered.terre.catalog.NamedCatalogType
import org.lanternpowered.terre.catalog.NamedCatalogTypeRegistry
import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.util.collection.toImmutableCollection
import org.lanternpowered.terre.util.collection.toImmutableMap
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

internal fun <T : NamedCatalogType> buildNamedCatalogTypeRegistryOf(
    type: KClass<T>, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): NamedCatalogTypeRegistryImpl<T> {
  val builder = NamedCatalogTypeRegistryBuilderImpl<T>()
  fn(builder)
  return NamedCatalogTypeRegistryImpl(type, builder.byName.toImmutableMap())
}

internal inline fun <reified T : NamedCatalogType> buildNamedCatalogTypeRegistryOf(
    noinline autoRegisterFunction: (String) -> T?, noinline fn: CatalogTypeRegistryBuilder<T>.() -> Unit = {}
) = buildNamedCatalogTypeRegistryOf(T::class, autoRegisterFunction, fn)

internal fun <T : NamedCatalogType> buildNamedCatalogTypeRegistryOf(
    type: KClass<T>, autoRegisterFunction: (String) -> T?, fn: CatalogTypeRegistryBuilder<T>.() -> Unit
): NamedCatalogTypeRegistryImpl<T> {
  val builder = NamedCatalogTypeRegistryBuilderImpl<T>()
  fn(builder)
  return NamedCatalogTypeRegistryImpl(type, ConcurrentHashMap(builder.byName), autoRegisterFunction)
}

internal open class NamedCatalogTypeRegistryBuilderImpl<T : NamedCatalogType> : CatalogTypeRegistryBuilder<T> {

  val byName = hashMapOf<String, T>()

  override fun register(catalogType: T) {
    check(!this.byName.containsKey(catalogType.name)) { "The name '${catalogType.name}' is already in use." }
    this.byName[catalogType.name.toLowerCase(Locale.ROOT)] = catalogType
  }
}

internal class NamedCatalogTypeRegistryImpl<T : NamedCatalogType>(
    private val type: KClass<T>,
    private val byName: MutableMap<String, T>,
    private val autoRegisterFunction: ((String) -> T?)? = null
) : NamedCatalogTypeRegistry<T> {

  override val all: Collection<T>
    get() = this.byName.values.toImmutableCollection()

  override fun get(name: String): T? {
    val key = name.toLowerCase(Locale.ROOT)
    var catalogType = this.byName[key]
    val autoRegisterFunction = this.autoRegisterFunction
    if (catalogType != null || autoRegisterFunction == null) {
      return catalogType
    }
    catalogType = autoRegisterFunction(name) ?: return null
    this.byName[key] = catalogType
    return catalogType
  }

  override fun require(name: String): T =
      this[name] ?: throw IllegalArgumentException("${type.simpleName} with name '$name' doesn't exist.")
}
