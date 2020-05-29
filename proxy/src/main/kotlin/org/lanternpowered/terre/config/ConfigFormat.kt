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
import com.uchuhimo.konf.source.Writer
import com.uchuhimo.konf.source.Loader as Reader

/**
 * Represents a configuration format.
 */
interface ConfigFormat {

  /**
   * The default extension name.
   */
  val extension: String

  /**
   * Constructs a reader for the given [Config].
   */
  fun readerFor(config: Config): Reader

  /**
   * Constructs a writer for the given [Config].
   */
  fun writerFor(config: Config): Writer
}
