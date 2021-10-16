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

/**
 * Represents a configuration directory.
 */
interface ConfigDirectory : ConfigDirectoryBase {

  /**
   * Initializes a new config with the default name.
   *
   * @param format The format used to write and read the file
   * @param extension The extension of the file, defaults to the extension of the config format
   * @param initializer Function to initialize the config
   */
  fun config(
    format: ConfigFormat = ConfigFormats.Yaml,
    extension: String = format.extension,
    initializer: Config.() -> Unit
  ) = config("config", format, extension, initializer)
}
