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
import com.uchuhimo.konf.source.hocon
import com.uchuhimo.konf.source.hocon.toHocon
import com.uchuhimo.konf.source.json.toJson
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.source.toml.toToml
import org.lanternpowered.terre.impl.config.BetterHoconWriter

object ConfigFormats {

  val Json = object : ConfigFormat {
    override val extension get() = "json"
    override fun readerFor(config: Config) = config.from.json
    override fun writerFor(config: Config) = config.toJson
  }

  val Hocon = object : ConfigFormat {
    override val extension get() = "conf"
    override fun readerFor(config: Config) = config.from.hocon
    override fun writerFor(config: Config) = BetterHoconWriter(config.toHocon, indentSize = 2)
  }
}
