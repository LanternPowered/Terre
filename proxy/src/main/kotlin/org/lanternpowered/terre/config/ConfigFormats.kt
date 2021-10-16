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
import com.uchuhimo.konf.source.json.toJson
import com.uchuhimo.konf.source.yaml
import com.uchuhimo.konf.source.yaml.toYaml

object ConfigFormats {

  val Json = object : ConfigFormat {
    override val extension get() = "json"
    override fun readerFor(config: Config) = config.from.json
    override fun writerFor(config: Config) = config.toJson
  }

  val Yaml = object : ConfigFormat {
    override val extension get() = "yaml"
    override fun readerFor(config: Config) = config.from.yaml
    override fun writerFor(config: Config) = config.toYaml
  }
}
