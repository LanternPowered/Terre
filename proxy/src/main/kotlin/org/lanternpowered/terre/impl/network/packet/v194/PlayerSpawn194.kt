/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v194

import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readShortVec2i
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeShortVec2i
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal val PlayerSpawn194Encoder = PacketEncoder<PlayerSpawnPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortVec2i(packet.position)
}

internal val PlayerSpawn194Decoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val position = buf.readShortVec2i()
  PlayerSpawnPacket(playerId, position, 0, PlayerSpawnPacket.Context.SpawningIntoWorld)
}
