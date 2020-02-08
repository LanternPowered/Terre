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

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class PlayerHealthPacket(
    val playerId: PlayerId,
    val current: Int,
    val max: Int
) : Packet

internal val PlayerHealthDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val current = buf.readShortLE().toInt()
  val max = buf.readShortLE().toInt()
  PlayerHealthPacket(playerId, current, max)
}

internal val PlayerHealthEncoder = packetEncoderOf<PlayerHealthPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortLE(packet.current)
  buf.writeShortLE(packet.max)
}
