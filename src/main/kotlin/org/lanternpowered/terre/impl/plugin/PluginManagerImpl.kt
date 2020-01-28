/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.plugin

import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.plugin.PluginManager
import org.lanternpowered.terre.util.collection.toImmutableList
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

internal class PluginManagerImpl : PluginManager {

  private val idToContainers = ConcurrentHashMap<String, TerrePluginContainer>()
  private val instanceToContainers = ConcurrentHashMap<Any, TerrePluginContainer>()
  private val classToContainers = ConcurrentHashMap<KClass<*>, TerrePluginContainer>()

  override val all: List<PluginContainer>
    get() = this.instanceToContainers.values.toImmutableList()

  override fun get(id: String): PluginContainer?
      = this.idToContainers[id.toLowerCase()]

  /**
   * Loads all the plugins from the target directory.
   */
  fun load(directory: Path) {

  }

  /**
   * Attempts to get the [PluginContainer] for the given plugin instance.
   */
  fun getPluginContainer(instance: Any): TerrePluginContainer? {
    if (instance is TerrePluginContainer)
      return instance

    val kClass = instance::class

    val container = this.instanceToContainers[instance]
    if (container != null)
      return container

    // Find the annotation, if there's no annotation, it's not a plugin
    val annotation = kClass.findAnnotation<Plugin>() ?: return null
    return addOrGetPluginContainer(annotation, instance)
  }

  private fun addOrGetPluginContainer(annotation: Plugin, instance: Any): TerrePluginContainer {
    return this.instanceToContainers.computeIfAbsent(instance) {
      createPluginContainer(annotation, instance).also {
        this.classToContainers[instance::class] = it
        this.idToContainers[it.id] = it
      }
    }
  }

  private fun createPluginContainer(annotation: Plugin, instance: Any): TerrePluginContainer {
    check(annotation.id.isNotBlank()) { "The plugin id cannot be blank" }
    val name = if (annotation.name.isBlank()) annotation.id else annotation.name
    val version = if (annotation.version.isBlank()) null else annotation.version
    val description = if (annotation.description.isBlank()) null else annotation.description
    val url = if (annotation.url.isBlank()) null else annotation.url
    val authors = annotation.authors.toList()
    return TerrePluginContainer(annotation.id, name, version, description, authors, url, instance)
  }
}
