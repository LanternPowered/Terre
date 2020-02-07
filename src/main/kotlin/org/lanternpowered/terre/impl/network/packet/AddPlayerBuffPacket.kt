/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class AddPlayerBuffPacket(
    val playerId: PlayerId,
    val buff: Int,
    val time: Int
) : Packet

internal val AddPlayerBuffEncoder = AddPlayerBuffEncoder(Int.MAX_VALUE)

internal fun AddPlayerBuffEncoder(protocol: Int) = packetEncoderOf<AddPlayerBuffPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.buff)
  if (protocol == 155) {
    buf.writeShortLE(packet.time)
  } else {
    buf.writeIntLE(packet.time)
  }
}

internal val AddPlayerBuffDecoder = AddPlayerBuffDecoder(Int.MAX_VALUE)

internal fun AddPlayerBuffDecoder(protocol: Int) = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val buff = buf.readByte().toInt()
  val time = if (protocol == 155) buf.readShortLE().toInt() else buf.readIntLE()
  AddPlayerBuffPacket(playerId, buff, time)
}
