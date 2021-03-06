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

import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.plugin.PluginManager
import org.lanternpowered.terre.util.collection.toImmutableList
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
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
  fun load(scanClasspath: Boolean = true) {
    val pluginsFolder = Paths.get("plugins")

    val pluginScanner = PluginScanner()
    if (scanClasspath) {
      Terre.logger.info("Scanning classpath for plugins...")
      val loader = this::class.java.classLoader
      if (loader is URLClassLoader) {
        pluginScanner.scanClassPath(loader)
      } else {
        Terre.logger.error("Cannot search for plugins on classpath: Unsupported class loader: {}",
            loader.javaClass.name)
      }
    }

    if (Files.exists(pluginsFolder)) {
      pluginScanner.scanDirectory(pluginsFolder)
    } else {
      Files.createDirectories(pluginsFolder)
    }

    val candidates = pluginScanner.plugins
    Terre.logger.info("${candidates.size} plugin(s) found")

    try {
      for (candidate in candidates) {
        val pluginClass = Class.forName(candidate.className).kotlin
        val instance = pluginClass.objectInstance ?: continue
        addOrGetPluginContainer(pluginClass.findAnnotation()!!, instance)
        EventBus.register(instance)
      }
    } catch (e: Throwable) {
      throw RuntimeException("An error occurred while loading the plugins", e)
    }
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
    // TODO: Plugin metadata, lets wait for the sponge plugin metadata library
    return TerrePluginContainer(annotation.id, instance = instance)
  }
}
