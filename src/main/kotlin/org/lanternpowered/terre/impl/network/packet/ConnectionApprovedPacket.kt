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

internal data class ConnectionApprovedPacket(val playerId: PlayerId) : Packet

internal val ConnectionApprovedEncoder = packetEncoderOf<ConnectionApprovedPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
}

internal val ConnectionApprovedDecoder = packetDecoderOf { buf -> ConnectionApprovedPacket(buf.readPlayerId()) }
