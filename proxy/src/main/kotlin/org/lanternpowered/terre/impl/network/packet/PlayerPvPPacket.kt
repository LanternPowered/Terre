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
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId

internal data class PlayerPvPPacket(
  val playerId: PlayerId,
  val enabled: Boolean,
) : Packet

internal val PlayerPvPEncoder = PacketEncoder<PlayerPvPPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeBoolean(packet.enabled)
}

internal val PlayerPvPDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val enabled = buf.readBoolean()
  PlayerPvPPacket(playerId, enabled)
}
