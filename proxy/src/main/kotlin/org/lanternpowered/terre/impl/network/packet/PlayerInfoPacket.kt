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

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.ForwardingReferenceCounted
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.calculateLength
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.util.toString

internal class PlayerInfoPacket(
  val playerId: PlayerId,
  val playerName: String,
  val data: ByteBuf
) : Packet, ForwardingReferenceCounted(data) {

  override fun toString() = toString {
    "playerId" to playerId
    "playerName" to playerName
  }
}

private val idToNameOffset = calculateLength {
  byte() // skin variant
  byte() // hair
}

internal val PlayerInfoDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val index = buf.readerIndex()
  buf.skipBytes(idToNameOffset)
  val playerName = buf.readString()

  val size = buf.readableBytes() + idToNameOffset
  val data = byteBufAllocator.buffer(size)
  buf.readBytes(data, idToNameOffset, buf.readableBytes())
  buf.readerIndex(index)
  buf.readBytes(data, 0, idToNameOffset)
  data.writerIndex(size)

  PlayerInfoPacket(playerId, playerName, data)
}

internal val PlayerInfoEncoder = PacketEncoder<PlayerInfoPacket> { buf, packet ->
  val data = packet.data
  buf.writePlayerId(packet.playerId)
  buf.writeBytes(data, 0, idToNameOffset)
  buf.writeString(packet.playerName)
  buf.writeBytes(data, idToNameOffset, data.readableBytes() - idToNameOffset)
}
