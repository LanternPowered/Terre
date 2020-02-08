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
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.calculateLength
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.util.toString

internal class PlayerInfoPacket(
    val playerId: PlayerId,
    val playerName: String,
    val data: ByteArray
) : Packet {

  override fun toString() = toString {
    "playerId" to playerId
    "playerName" to playerName
  }
}

private val idToNameOffset = calculateLength {
  byte() // skin variant
  byte() // hair
}

internal val PlayerInfoDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val index = buf.readerIndex()
  buf.skipBytes(idToNameOffset)
  val playerName = buf.readString()

  val data = ByteArray(buf.readableBytes() + idToNameOffset)
  buf.readBytes(data, idToNameOffset, buf.readableBytes())
  buf.readerIndex(index)
  buf.readBytes(data, 0, idToNameOffset)

  PlayerInfoPacket(playerId, playerName, data)
}

internal val PlayerInfoEncoder = packetEncoderOf<PlayerInfoPacket> { buf, packet ->
  val data = packet.data
  buf.writePlayerId(packet.playerId)
  buf.writeBytes(data, 0, idToNameOffset)
  buf.writeString(packet.playerName)
  buf.writeBytes(data, idToNameOffset, data.size - idToNameOffset)
}
