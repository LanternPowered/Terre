/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v238

import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readShortVec2i
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeShortVec2i
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket

private val contextById = PlayerSpawnPacket.Context.values()

internal val PlayerSpawn238Encoder = PacketEncoder<PlayerSpawnPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortVec2i(packet.position)
  buf.writeIntLE(packet.respawnTimeRemaining)
  buf.writeByte(packet.respawnContext.ordinal)
}

internal val PlayerSpawn238Decoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val position = buf.readShortVec2i()
  val respawnTimeRemaining = buf.readIntLE()
  val respawnContext = contextById[buf.readUnsignedByte().toInt()]
  PlayerSpawnPacket(playerId, position, respawnTimeRemaining, 0, 0,
    respawnContext)
}
