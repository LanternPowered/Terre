/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.stream.JsonReader
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import java.io.InputStreamReader

internal object TileInfo {

  private val frameImportant: IntSet

  init {
    val input = TileInfo::class.java
      .getResourceAsStream("/data/tile_frame_important.json")
      ?: error("Failed to find tile frame important data.")
    val reader = JsonReader(InputStreamReader(input))
    reader.use {
      frameImportant = Gson().fromJson<JsonArray>(reader, JsonArray::class.java)
        .map { element -> element.asInt }
        .toCollection(IntOpenHashSet())
    }
  }

  fun isFrameImportantFor(tileId: Int): Boolean =
    frameImportant.contains(tileId)
}
