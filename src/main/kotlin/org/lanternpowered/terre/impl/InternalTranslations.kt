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
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import java.io.InputStreamReader

object InternalTranslations {

  private val gson = Gson()

  fun loadIndexedTranslations(type: String): Int2ObjectMap<String> {
    val input = InternalTranslations::class.java.getResourceAsStream(
        "/internal/translations/$type/en_us.json")
    val array = InputStreamReader(input).use {
      gson.fromJson(it, JsonArray::class.java)
    }
    val mappings = Int2ObjectOpenHashMap<String>()
    for (element in array) {
      element as JsonObject
      mappings[element["i"].asInt] = element["v"].asString
    }
    return mappings
  }

  fun loadNamedTranslations(type: String): Object2ObjectMap<String, String> {
    val input = InternalTranslations::class.java.getResourceAsStream(
        "/internal/translations/$type/en_us.json")
    val json = InputStreamReader(input).use {
      gson.fromJson(it, JsonObject::class.java)
    }
    val mappings = Object2ObjectOpenHashMap<String, String>()
    for (entry in json.entrySet()) {
      mappings[entry.key] = entry.value.asString
    }
    return mappings
  }
}
