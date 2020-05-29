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

internal data class PlayerActivePacket(
    val playerId: PlayerId,
    val active: Boolean
) : Packet

internal val PlayerActiveEncoder = packetEncoderOf<PlayerActivePacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeBoolean(packet.active)
}

internal val PlayerActiveDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val active = buf.readBoolean()
  PlayerActivePacket(playerId, active)
}
