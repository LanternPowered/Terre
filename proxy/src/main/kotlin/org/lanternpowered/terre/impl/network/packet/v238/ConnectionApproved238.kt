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
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket

internal val ConnectionApproved238Encoder = PacketEncoder<ConnectionApprovedPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
}

internal val ConnectionApproved238Decoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  ConnectionApprovedPacket(playerId)
}
