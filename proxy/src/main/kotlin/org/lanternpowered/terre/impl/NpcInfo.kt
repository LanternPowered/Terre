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
import org.lanternpowered.terre.impl.network.buffer.NpcType
import java.io.InputStreamReader

internal object NpcInfo {

  private val catchable: IntSet

  init {
    val input = NpcInfo::class.java
      .getResourceAsStream("/data/npc_catchable.json")
      ?: error("Failed to find catchable npc data.")
    val reader = JsonReader(InputStreamReader(input))
    reader.use {
      catchable = Gson().fromJson<JsonArray>(reader, JsonArray::class.java)
        .map { element -> element.asInt }
        .toCollection(IntOpenHashSet())
    }
  }

  fun isCatchable(npcType: NpcType): Boolean =
    catchable.contains(npcType.value)
}
