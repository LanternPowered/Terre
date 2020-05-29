/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v155

import org.lanternpowered.terre.impl.InternalTranslations
import org.lanternpowered.terre.impl.network.PacketCodecContext
import org.lanternpowered.terre.impl.network.cache.DeathSourceInfoCache
import org.lanternpowered.terre.impl.network.packet.PlayerDamageReason
import kotlin.random.Random

private val npcNameMappings = InternalTranslations.loadIndexedTranslations("npc")
private val itemNameMappings = InternalTranslations.loadIndexedTranslations("item")
private val projectileNameMappings = InternalTranslations.loadIndexedTranslations("projectile")
private val deathTranslations = InternalTranslations.loadNamedTranslations("death")

private fun format(key: String, vararg args: Any): String {
  val format = deathTranslations[key]
  return if (format == null) key else String.format(format, *args)
}

internal fun PlayerDamageReason.toDeathMessage(context: PacketCodecContext): String {
  if (custom != null)
    return custom

  val cache: DeathSourceInfoCache? = context.connection.attr(DeathSourceInfoCache.Attribute).get()
  val projectileName = if (projectile != null) projectileNameMappings[projectile.type.value] ?: null else null

  val npcName = if (npcId != null && cache != null) {
    val npc = cache.npcs[npcId]
    npc.name ?: npcNameMappings[npc.type.value]
  } else null

  val itemName = if (item != null) itemNameMappings[item.item.numericId] else null

  val playerName = if (playerId != null && cache != null) {
    cache.players.names[playerId] ?: "Unknown"
  } else null

  val deadPlayerName = cache?.playerName ?: "Unknown"
  val worldName = cache?.worldName ?: "Unknown"

  fun randomGenericText() = format("generic_${Random.nextInt(1, 27)}", deadPlayerName, worldName)

  if (playerName != null) {
    val weapon = projectileName ?: itemName ?: "Touch of Death"
    return format("player", randomGenericText(), playerName, weapon)
  }

  if (npcName != null)
    return format("npc", randomGenericText(), npcName)

  if (projectileName != null)
    return format("projectile", randomGenericText(), projectileName)

  fun formatSimple(key: String) = format(key, deadPlayerName, worldName)
  return when (other) {
    PlayerDamageReason.Other.Falling -> formatSimple("fell_${Random.nextInt(1, 3)}")
    PlayerDamageReason.Other.Drowning -> formatSimple("drowned_${Random.nextInt(1, 5)}")
    PlayerDamageReason.Other.Lava -> formatSimple("lava_${Random.nextInt(1, 5)}")
    PlayerDamageReason.Other.Default -> format("default", randomGenericText())
    PlayerDamageReason.Other.Slain -> formatSimple("slain")
    PlayerDamageReason.Other.Petrified -> formatSimple("petrified_${Random.nextInt(1, 5)}")
    PlayerDamageReason.Other.Stabbed -> formatSimple("stabbed")
    PlayerDamageReason.Other.Suffocation -> formatSimple("suffocated")
    PlayerDamageReason.Other.Burning -> formatSimple("burned")
    PlayerDamageReason.Other.Poison -> formatSimple("poisoned")
    PlayerDamageReason.Other.Electrified -> formatSimple("electrocuted")
    PlayerDamageReason.Other.TriedToEscape -> formatSimple("tried_to_escape")
    PlayerDamageReason.Other.WasLicked -> formatSimple("was_licked")
    PlayerDamageReason.Other.ChaosState -> formatSimple("teleport_1")
    PlayerDamageReason.Other.MaleChaosState -> formatSimple("teleport_2_male")
    PlayerDamageReason.Other.FemaleChaosState -> formatSimple("teleport_2_female")
    else -> formatSimple("slain")
  }
}
