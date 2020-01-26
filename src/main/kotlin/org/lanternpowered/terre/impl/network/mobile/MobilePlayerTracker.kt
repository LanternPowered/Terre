/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.mobile

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.lanternpowered.terre.impl.math.distanceSquared

/**
 * The mobile player tracker is an object that tracks server side
 * players for a mobile client. The mobile client has a limited set
 * of available player slots, 16 compared to 255 on desktop. In order
 * to allow more than 16 players, 15 slots needs to be shared by 254
 * players that aren't the tracking player.
 *
 * Player id 1 will always be the connecting
 * client. 2-15 will be other players.
 *
 * @property clientPlayer The player that is tracking the other players
 */
internal class MobilePlayerTracker {

  private lateinit var clientPlayer: TrackedMobilePlayer

  private val byServerId = Int2ObjectOpenHashMap<TrackedMobilePlayer>()
  private val byMobileId = Array<TrackedMobilePlayer?>(16) { null }

  fun initClientPlayer(clientPlayer: TrackedMobilePlayer) {
    this.clientPlayer = clientPlayer
    this.byServerId[clientPlayer.playerId.value] = clientPlayer
    assignMobileId(0, clientPlayer)
  }

  /**
   * Updates the players that should be visible on mobile.
   */
  fun updateVisible() {
    if (!this::clientPlayer.isInitialized)
      return

    val clientPos = this.clientPlayer.position

    // Find the 16 closest players, they should be prioritized to be visible,
    // this includes the client player itself
    val players = this.byServerId.values.asSequence()
        .sortedBy { distanceSquared(clientPos, it.position) }
        .take(this.byMobileId.size)
        .toList()

    // The tracked mobile player entries that got replaced by another one,
    // all packets should be send for the replacement players to update the
    // client.
    val replacements = mutableListOf<Pair<TrackedMobilePlayer?, TrackedMobilePlayer>>()

    // Player to add
    val toAdd = mutableListOf<TrackedMobilePlayer>()
    val toRemove = mutableListOf<TrackedMobilePlayer>()

    for ((_, player) in this.byServerId.int2ObjectEntrySet()) {
      val mobileId = player.mobileId

      // Check if the player should be visible
      if (player in players) {
        // Not yet visible
        if (mobileId == -1) {
          toAdd += player
        }
        // Already visible, but shouldn't be any longer
      } else if (mobileId != -1) {
        toRemove += player
      }
    }

    var freeCheckOffset = 1 // Skip 0, is allocated for the client player
    // Finds the next available mobile id.
    fun nextFreeMobileId(): Int {
      for (i in freeCheckOffset until this.byMobileId.size) {
        if (this.byMobileId[i] == null) {
          // Increase the offset to reduce checking next time
          freeCheckOffset = i + 1
          return i
        }
      }
      error("There are no free mobile ids.")
    }

    for (player in toAdd) {
      replacements += if (toRemove.isNotEmpty()) {
        val removed = toRemove.removeAt(0)
        assignMobileId(removed.mobileId, player)
        removed to player
      } else {
        assignMobileId(nextFreeMobileId(), player)
        null to player
      }
    }
  }

  private fun assignMobileId(id: Int, player: TrackedMobilePlayer) {
    check(id in this.byMobileId.indices)
    // Reset the old player id
    this.byMobileId[id]?.mobileId = -1
    // Update the new one and assign the id
    player.mobileId = id
    this.byMobileId[id] = player
  }
}

