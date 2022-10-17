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
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class ConnectionApprovedPacket(val playerId: PlayerId) : Packet

internal val ConnectionApprovedEncoder = PacketEncoder<ConnectionApprovedPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeBoolean(false) // serverWantsToRunCheckBytesInClientLoopThread -> disable
}

internal val ConnectionApprovedDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  // if condition for server-init protocol
  if (buf.readableBytes() > 0)
    buf.readBoolean() // serverWantsToRunCheckBytesInClientLoopThread -> ignore
  ConnectionApprovedPacket(playerId)
}
