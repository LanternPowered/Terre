/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.buffer.Projectile
import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.ProjectileType
import org.lanternpowered.terre.impl.network.buffer.readNpcId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readProjectileId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.readUByte
import org.lanternpowered.terre.impl.network.buffer.writeNpcId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeProjectileId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.item.ItemModifierRegistry
import org.lanternpowered.terre.item.ItemTypeRegistry
import org.lanternpowered.terre.item.ItemStack

/**
 * A packet when a player gets hurt.
 */
internal data class PlayerHurtPacket(
  val playerId: PlayerId,
  val damage: Int,
  val hitDirection: Int,
  val critical: Boolean,
  val pvp: Boolean,
  val cooldownCounter: Int,
  val reason: PlayerDamageReason
) : Packet

internal val PlayerHurtEncoder = PacketEncoder<PlayerHurtPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeDamageReason(packet.reason)
  buf.writeShortLE(packet.damage)
  buf.writeByte(packet.hitDirection)
  var flags = 0
  if (packet.critical)
    flags += 0x1
  if (packet.pvp)
    flags += 0x2
  buf.writeByte(packet.cooldownCounter)
}

internal val PlayerHurtDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val reason = buf.readDamageReason()
  val damage = buf.readUnsignedShortLE()
  val hitDirection = buf.readByte().toInt()
  val flags = buf.readByte().toInt()
  val critical = (flags and 0x1) != 0
  val pvp = (flags and 0x2) != 0
  val cooldownCounter = buf.readByte().toInt()
  PlayerHurtPacket(playerId, damage, hitDirection, critical, pvp, cooldownCounter, reason)
}

internal data class PlayerDamageReason(
  val playerId: PlayerId? = null,
  val npcId: NpcId? = null,
  val projectile: Projectile? = null,
  val other: Other? = null,
  val item: ItemStack? = null,
  val custom: String? = null
) {

  enum class Other {
    Falling,
    Drowning,
    Lava,
    Default,
    Slain,
    Petrified,
    Stabbed,
    Suffocation,
    Burning,
    Poison,
    Electrified,
    TriedToEscape,
    WasLicked,
    ChaosState,
    MaleChaosState,
    FemaleChaosState
  }
}

private val otherValues = PlayerDamageReason.Other.values()

internal fun ByteBuf.readDamageReason(): PlayerDamageReason {
  val flags = readByte().toInt()
  val playerId = if ((flags and 0x1) != 0) readPlayerId() else null
  val npcId = if ((flags and 0x2) != 0) readNpcId() else null
  val projectileId = if ((flags and 0x4) != 0) readProjectileId() else null
  val other = if ((flags and 0x8) != 0) otherValues[readUByte().toInt()] else null
  val projectileType = if ((flags and 0x10) != 0) readShortLE().toInt() else null
  val projectile = if (projectileId != null && projectileType != null)
    Projectile(projectileId, ProjectileType(projectileType)) else null
  val itemId = if ((flags and 0x20) != 0) readShortLE().toInt() else null
  val itemModifier = if ((flags and 0x40) != 0) readByte().toInt() else null
  val itemStack = if (itemId != null) ItemStack(ItemTypeRegistry.require(itemId),
    ItemModifierRegistry.require(itemModifier ?: 0)) else null
  val custom = if ((flags and 0x80) != 0) readString() else null
  return PlayerDamageReason(playerId, npcId, projectile, other, itemStack, custom)
}

internal fun ByteBuf.writeDamageReason(reason: PlayerDamageReason) {
  val playerId = reason.playerId
  val npcId = reason.npcId
  val projectile = reason.projectile
  val other = reason.other
  val item = reason.item
  val custom = reason.custom
  var flags = 0
  if (playerId != null)
    flags += 0x1
  if (npcId != null)
    flags += 0x2
  if (projectile != null) {
    flags += 0x4
    flags += 0x10
  }
  if (other != null)
    flags += 0x8
  if (item != null) {
    flags += 0x20
    flags += 0x40
  }
  if (custom != null)
    flags += 0x80
  writeByte(flags)
  if (playerId != null)
    writePlayerId(playerId)
  if (npcId != null)
    writeNpcId(npcId)
  if (projectile != null)
    writeProjectileId(projectile.id)
  if (other != null)
    writeByte(other.ordinal)
  if (projectile != null)
    writeShortLE(projectile.type.value)
  if (item != null) {
    writeShortLE(item.type.numericId)
    writeByte(item.modifier.numericId)
  }
  if (custom != null)
    writeString(custom)
}
