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

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.ForwardingReferenceCounted
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.readUUID
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.buffer.writeUUID
import org.lanternpowered.terre.impl.network.calculateLength
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.util.toString
import java.util.UUID

internal class WorldInfoPacket(
    val id: Int,
    val uniqueId: UUID,
    val name: String,
    val generatorVersion: Long,
    val gameMode: Int,
    val data: ByteBuf
) : Packet, ForwardingReferenceCounted(data) {

  override fun toString() = toString {
    "id" to id
    "name" to name
    "uniqueId" to uniqueId
    "generatorVersion" to generatorVersion
  }
}

private val idOffset = calculateLength {
  int() // time
  byte() // flags
  byte() // moon phase
  shortVec2i() // size
  shortVec2i() // spawn position
  short() // surface position
  short() // rock layer position
}

internal val WorldInfoEncoder = WorldInfoEncoder(Int.MAX_VALUE)

internal inline fun WorldInfoEncoder(protocol: Int) = packetEncoderOf<WorldInfoPacket> { buf, packet ->
  val data = packet.data
  buf.writeBytes(data, 0, idOffset)
  buf.writeIntLE(packet.id)
  buf.writeString(packet.name)
  if (protocol > 194)
    buf.writeByte(packet.gameMode)
  if (protocol != 155) {
    buf.writeUUID(packet.uniqueId)
    buf.writeLongLE(packet.generatorVersion)
  }
  buf.writeBytes(data, idOffset, data.readableBytes() - idOffset)
  // Last long is the steam lobby id, do we need to handle this?
}

internal val WorldInfoDecoder = WorldInfoDecoder(Int.MAX_VALUE)

private val EmptyUUID = UUID(0L, 0L)

internal inline fun WorldInfoDecoder(protocol: Int) = packetDecoderOf { buf ->
  buf.skipBytes(idOffset)

  val id = buf.readIntLE()
  val name = buf.readString()
  val gameMode = if (protocol > 194) buf.readUnsignedByte().toInt() else 0
  val uniqueId = if (protocol == 155) EmptyUUID else buf.readUUID()
  val generatorVersion = if (protocol == 155) 0L else buf.readLongLE()

  val size = idOffset + buf.readableBytes()
  val data = this.byteBufAllocator.buffer(size)
  // Read data after the generator version or name
  buf.readBytes(data, idOffset, buf.readableBytes())
  val end = buf.readerIndex()
  // Read data before the id
  buf.readerIndex(0)
  buf.readBytes(data, 0, idOffset)
  // Move reader index back to the end
  buf.readerIndex(end)
  data.writerIndex(size)

  WorldInfoPacket(id, uniqueId, name, generatorVersion, gameMode, data)
}
