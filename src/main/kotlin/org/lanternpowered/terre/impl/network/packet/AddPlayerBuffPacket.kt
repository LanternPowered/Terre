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
import org.lanternpowered.terre.impl.network.packetDecoderOf

internal data class AddPlayerBuffPacket(
    val playerId: PlayerId,
    val buff: Int,
    val time: Int
) : Packet

internal val AddPlayerBuffDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val buff = buf.readByte().toInt()
  val time = buf.readIntLE()
  AddPlayerBuffPacket(playerId, buff, time)
}
