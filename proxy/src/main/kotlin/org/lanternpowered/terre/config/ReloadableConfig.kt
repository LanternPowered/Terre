/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.config

import com.uchuhimo.konf.Config
import kotlinx.coroutines.Job
import java.nio.file.Path

/**
 * Represents a [Config] which is more convenient to work with.
 */
interface ReloadableConfig : Config {

  /**
   * Whether the configuration file exists.
   */
  val exists: Boolean

  /**
   * The path to the configuration file.
   */
  val path: Path

  /**
   * The configuration format.
   */
  val format: ConfigFormat

  /**
   * Loads or reloads the config file.
   */
  suspend fun load()

  /**
   * Loads, reloads or creates the config file.
   */
  suspend fun loadOrCreate()

  /**
   * Loads or reloads the config file.
   */
  fun loadAsync(): Job

  /**
   * Saves the config file.
   */
  suspend fun save()

  /**
   * Saves the config file.
   */
  fun saveAsync(): Job
}
