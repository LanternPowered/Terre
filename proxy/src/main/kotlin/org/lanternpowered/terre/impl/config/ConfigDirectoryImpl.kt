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

internal open class ConfigDirectoryBaseImpl(override val path: Path) : ConfigDirectoryBase {

  override fun config(
    name: String, format: ConfigFormat, extension: String, initializer: Config.() -> Unit
  ): ReloadableConfig {
    val config = Config().also(initializer)
    val path = path.resolve(name + if (extension.isNotBlank()) ".$extension" else "")
    return ReloadableConfigImpl(config, path, format)
  }
}

internal class RootConfigDirectoryImpl(path: Path) :
  ConfigDirectoryBaseImpl(path), RootConfigDirectory

internal class ConfigDirectoryImpl(path: Path) :
  ConfigDirectoryBaseImpl(path), ConfigDirectory
