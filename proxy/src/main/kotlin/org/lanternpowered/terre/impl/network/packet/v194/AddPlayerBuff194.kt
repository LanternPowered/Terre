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
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.packet.AddPlayerBuffPacket
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal val AddPlayerBuff194Encoder = packetEncoderOf<AddPlayerBuffPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.buff)
  buf.writeIntLE(packet.time)
}

internal val AddPlayerBuff194Decoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val buff = buf.readByte().toInt()
  val time = buf.readIntLE()
  AddPlayerBuffPacket(playerId, buff, time)
}
