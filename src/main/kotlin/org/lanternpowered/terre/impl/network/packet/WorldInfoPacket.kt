/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.Protocol155
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.readUUID
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.buffer.writeUUID
import org.lanternpowered.terre.impl.network.calculateLength
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import java.util.*

internal class WorldInfoPacket(
    val id: Int,
    val uniqueId: UUID,
    val name: String,
    val generatorVersion: Long,
    val data: ByteArray
) : Packet

private val idOffset = calculateLength {
  int() // time
  byte() // flags
  byte() // moon phase
  shortVec2i() // size
  shortVec2i() // spawn position
  short() // surface position
  short() // rock layer position
}

internal val WorldInfoEncoder = packetEncoderOf<WorldInfoPacket> { buf, packet ->
  val data = packet.data
  buf.writeBytes(data, 0, idOffset)
  buf.writeIntLE(packet.id)
  buf.writeString(packet.name)
  if (this.protocol != Protocol155) {
    buf.writeUUID(packet.uniqueId)
    buf.writeLongLE(packet.generatorVersion)
  }
  buf.writeBytes(data, idOffset, data.size)
  // Last long is the steam lobby id, do we need to handle this?
}

private val EmptyUUID = UUID(0L, 0L)

internal val WorldInfoDecoder = packetDecoderOf { buf ->
  buf.skipBytes(idOffset)

  val id = buf.readIntLE()
  val name = buf.readString()
  val uniqueId = if (this.protocol == Protocol155) EmptyUUID else buf.readUUID()
  val generatorVersion = if (this.protocol == Protocol155) 0L else buf.readLongLE()

  val after = buf.readableBytes()
  val data = ByteArray(idOffset + after)

  // Read data after the generator version or name
  buf.readBytes(data, 0, after)
  val end = buf.readerIndex()
  // Read data before the id
  buf.readerIndex(0)
  buf.readBytes(data, 0, idOffset)
  // Move reader index back to the end
  buf.readerIndex(end)

  WorldInfoPacket(id, uniqueId, name, generatorVersion, data)
}
