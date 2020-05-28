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

import org.lanternpowered.terre.impl.math.Vec2i
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readShortVec2i
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeShortVec2i
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class PlayerSpawnPacket(
    val playerId: PlayerId,
    val position: Vec2i,
    val respawnTimeRemaining: Int,
    val respawnContext: Context
) : Packet {

  enum class Context {
    ReviveFromDeath,
    SpawningIntoWorld,
    RecallFromItem,
  }
}

private val contextById = PlayerSpawnPacket.Context.values()

internal val PlayerSpawnEncoder = packetEncoderOf<PlayerSpawnPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortVec2i(packet.position)
  buf.writeIntLE(packet.respawnTimeRemaining)
  buf.writeByte(packet.respawnContext.ordinal)
}

internal val PlayerSpawnDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val position = buf.readShortVec2i()
  val respawnTimeRemaining = buf.readIntLE()
  val respawnContext = contextById[buf.readUnsignedByte().toInt()]
  PlayerSpawnPacket(playerId, position, respawnTimeRemaining, respawnContext)
}
