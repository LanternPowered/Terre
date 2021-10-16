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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.lanternpowered.terre.config.ConfigFormat
import org.lanternpowered.terre.config.ReloadableConfig
import org.lanternpowered.terre.dispatcher.launchAsync
import java.nio.file.Files
import java.nio.file.Path

internal class ReloadableConfigImpl(
  private val backing: Config,
  override val path: Path,
  override val format: ConfigFormat
) : ReloadableConfig, Config by backing {

  override val exists: Boolean
    get() = Files.exists(this.path)

  override suspend fun load() = loadAsync().join()

  override suspend fun loadOrCreate() {
    if (exists) {
      load()
    } else {
      save()
    }
  }

  override fun loadAsync(): Job {
    return launchAsync(Dispatchers.IO) {
      val loaded = Files.newBufferedReader(path).use {
        format.readerFor(backing).reader(it)
      }
      for (item in loaded.items)
        backing.rawSet(item, loaded[item])
    }
  }

  override suspend fun save() = saveAsync().join()

  override fun saveAsync(): Job {
    return launchAsync(Dispatchers.IO) {
      val parent = path.parent
      if (!Files.exists(parent))
        Files.createDirectories(parent)
      Files.newBufferedWriter(path).use {
        format.writerFor(backing).toWriter(it)
        it.flush()
      }
    }
  }
}
