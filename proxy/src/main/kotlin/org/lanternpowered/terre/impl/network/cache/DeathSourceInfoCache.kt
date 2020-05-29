/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.cache

import io.netty.util.AttributeKey

/**
 * The death source cache is used for translating death/hurt packets
 * from the new protocol to the legacy protocol.
 *
 * @property playerName The name of the player being hurt/dying
 * @property npcs A cache containing information about npcs
 * @property players A cache containing information about players
 */
internal class DeathSourceInfoCache(val playerName: String) {

  /**
   * NPC related caching.
   */
  val npcs = NpcCache()

  /**
   * Player related caching.
   */
  val players = PlayerCache()

  /**
   * The name of the world.
   */
  var worldName = "Unknown"

  companion object {

    val Attribute: AttributeKey<DeathSourceInfoCache> = AttributeKey.valueOf("dsi_cache_155")
  }
}
