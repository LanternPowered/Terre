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

internal data class PlayerHealthPacket(
  val playerId: PlayerId,
  val current: Int,
  val max: Int
) : Packet

internal val PlayerHealthDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val current = buf.readShortLE().toInt()
  val max = buf.readShortLE().toInt()
  PlayerHealthPacket(playerId, current, max)
}

internal val PlayerHealthEncoder = PacketEncoder<PlayerHealthPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeShortLE(packet.current)
  buf.writeShortLE(packet.max)
}
