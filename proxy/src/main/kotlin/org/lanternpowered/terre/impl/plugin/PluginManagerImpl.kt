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
    get() = instanceToContainers.values.toImmutableList()

  override fun get(id: String): PluginContainer? =
    idToContainers[id.lowercase()]

  /**
   * Loads all the plugins from the target directory.
   */
  fun load(scanClasspath: Boolean = true, disabledPlugins: Set<String>) {
    val pluginsFolder = Paths.get("plugins")

    val pluginScanner = PluginScanner()
    if (scanClasspath) {
      Terre.logger.info("Scanning classpath for plugins...")
      val classpath = System.getProperty("java.class.path")
      val separator = System.getProperty("path.separator")
      val paths = classpath.split(separator)
        .map { entry -> Paths.get(entry) }
      pluginScanner.scanClassPath(paths.map { it.toUri().toURL() })
    }

    if (Files.exists(pluginsFolder)) {
      pluginScanner.scanDirectory(pluginsFolder)
    } else {
      Files.createDirectories(pluginsFolder)
    }

    val candidates = pluginScanner.plugins
    Terre.logger.info("${candidates.size} plugin(s) found")

    try {
      val pluginClassLoader = PluginClassLoader()
      for (candidate in candidates) {
        if (disabledPlugins.contains(candidate.id)) {
          Terre.logger.info("Plugin ${candidate.id} is disabled, skipping...")
          continue
        }
        val url = candidate.source?.toUri()?.toURL()
        if (url != null)
          pluginClassLoader.addURL(url)
        val pluginClass = Class.forName(candidate.className, true, pluginClassLoader).kotlin
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

    val container = instanceToContainers[instance]
    if (container != null)
      return container

    // Find the annotation, if there's no annotation, it's not a plugin
    val annotation = kClass.findAnnotation<Plugin>() ?: return null
    return addOrGetPluginContainer(annotation, instance)
  }

  private fun addOrGetPluginContainer(annotation: Plugin, instance: Any): TerrePluginContainer {
    return instanceToContainers.computeIfAbsent(instance) {
      createPluginContainer(annotation, instance).also {
        classToContainers[instance::class] = it
        idToContainers[it.id] = it
      }
    }
  }

  private fun createPluginContainer(annotation: Plugin, instance: Any): TerrePluginContainer {
    check(annotation.id.isNotBlank()) { "The plugin id cannot be blank" }
    // TODO: Plugin metadata, lets wait for the sponge plugin metadata library
    return TerrePluginContainer(annotation.id, instance = instance)
  }
}
