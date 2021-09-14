/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.config

import com.uchuhimo.konf.Config
import org.lanternpowered.terre.config.ConfigDirectory
import org.lanternpowered.terre.config.ConfigDirectoryBase
import org.lanternpowered.terre.config.ConfigFormat
import org.lanternpowered.terre.config.ReloadableConfig
import org.lanternpowered.terre.config.RootConfigDirectory
import java.nio.file.Path

open class ConfigDirectoryBaseImpl(override val path: Path) : ConfigDirectoryBase {

  override fun config(
    name: String, format: ConfigFormat, extension: String, fn: Config.() -> Unit
  ): ReloadableConfig {
    val config = Config().also(fn)
    val path = path.resolve(name + if (extension.isNotBlank()) ".$extension" else "")
    return ReloadableConfigImpl(config, path, format)
  }
}

class RootConfigDirectoryImpl(path: Path) : ConfigDirectoryBaseImpl(path), RootConfigDirectory

class ConfigDirectoryImpl(path: Path) : ConfigDirectoryBaseImpl(path), ConfigDirectory
