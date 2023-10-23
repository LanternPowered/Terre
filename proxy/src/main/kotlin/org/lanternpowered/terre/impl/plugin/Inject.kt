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

import kotlinx.coroutines.CoroutineDispatcher
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.command.CommandManager
import org.lanternpowered.terre.config.ConfigDirectory
import org.lanternpowered.terre.config.RootConfigDirectory
import org.lanternpowered.terre.event.EventBus
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.plugin.PluginManager
import org.lanternpowered.terre.sql.SqlManager
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> inject(instance: Any?, type: Class<T>): T =
  instance.inject(type.kotlin, false) as T

@PublishedApi
internal fun Any?.inject(type: KType): Any? =
  inject(type.classifier as KClass<*>, type.isMarkedNullable)

/**
 * Injects objects.
 */
private fun Any?.inject(kClass: KClass<*>, isNullable: Boolean): Any? {
  val value = when (kClass) {
    RootConfigDirectory::class -> RootConfigDirectory
    EventBus::class -> EventBus
    Proxy::class -> Proxy
    PluginManager::class -> PluginManager
    Console::class -> Console
    CommandManager::class -> CommandManager
    CoroutineDispatcher::class -> Proxy.dispatcher
    SqlManager::class -> SqlManager
    else -> {
      val pluginContainer = if (this != null) {
        ProxyImpl.pluginManager.getByInstance(this)
      } else null ?: activePlugin as? TerrePluginContainer
      pluginContainer?.inject(kClass)
    }
  }
  if (value == null && !isNullable)
    error("Cannot retrieve ${kClass.qualifiedName} within the current context.")
  return value
}

/**
 * Injects plugin related objects.
 */
private fun TerrePluginContainer.inject(kClass: KClass<*>): Any? {
  @Suppress("DEPRECATION")
  return when (kClass) {
    PluginContainer::class -> this
    Logger::class, org.apache.logging.log4j.Logger::class -> this.logger
    java.util.logging.Logger::class -> this.javaLogger
    ConfigDirectory::class -> this.configDirectory
    else -> null
  }
}
