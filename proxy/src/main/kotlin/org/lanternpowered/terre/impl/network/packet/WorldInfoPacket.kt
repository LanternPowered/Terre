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

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.ForwardingReferenceCounted
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.readUUID
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.buffer.writeUUID
import org.lanternpowered.terre.impl.network.calculateLength
import org.lanternpowered.terre.util.toString
import java.util.UUID

internal data class WorldInfoPacket(
  val id: Int,
  val uniqueId: UUID,
  val name: String,
  val generatorVersion: Long,
  val gameMode: Int,
  val serverSideCharacter: Boolean,
  val data: ByteBuf
) : Packet, ForwardingReferenceCounted(data) {

  override fun toString() = toString {
    "id" to id
    "name" to name
    "uniqueId" to uniqueId
    "generatorVersion" to generatorVersion
    "gameMode" to gameMode
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

private val generatorVersionToEventInfoOffset = calculateLength {
  byte() // moon type
  byte() // tree background 1
  byte() // tree background 2
  byte() // tree background 3
  byte() // tree background 4
  byte() // corruption background
  byte() // jungle background
  byte() // snow background
  byte() // hallow background
  byte() // crimson background
  byte() // desert background
  byte() // ocean background
  byte() // mushroom background
  byte() // underworld background
  byte() // ice back style
  byte() // jungle back style
  byte() // hell back style
  float() // wind
  byte() // cloud number
  int() // tree 1
  int() // tree 2
  int() // tree 3
  byte() // tree style 1
  byte() // tree style 2
  byte() // tree style 3
  byte() // tree style 4
  int() // cave back 1
  int() // cave back 2
  int() // cave back 3
  byte() // cave back style 1
  byte() // cave back style 2
  byte() // cave back style 3
  byte() // cave back style 4
  byte() // forest tree top style 1
  byte() // forest tree top style 2
  byte() // forest tree top style 3
  byte() // forest tree top style 4
  byte() // corruption tree top style
  byte() // jungle tree top style
  byte() // snow tree top style
  byte() // hallow tree top style
  byte() // crimson tree top style
  byte() // desert tree top style
  byte() // ocean tree top style
  byte() // glowing mushroom tree top style
  byte() // underground tree top style
  float() // rain
}

internal val WorldInfoEncoder = PacketEncoder<WorldInfoPacket> { buf, packet ->
  val data = packet.data
  buf.writeBytes(data, 0, idOffset)
  buf.writeIntLE(packet.id)
  buf.writeString(packet.name)
  buf.writeByte(packet.gameMode)
  buf.writeUUID(packet.uniqueId)
  buf.writeLongLE(packet.generatorVersion)
  val index = buf.writerIndex()
  buf.writeBytes(data, idOffset, data.readableBytes() - idOffset)
  // Apply the serverSideCharacter bit if applicable
  val eventInfoIndex = index + generatorVersionToEventInfoOffset
  if (packet.serverSideCharacter)
    buf.setByte(eventInfoIndex, buf.getUnsignedByte(eventInfoIndex).toInt() or 0x40)
}

internal val WorldInfoDecoder = PacketDecoder { buf ->
  buf.skipBytes(idOffset)

  val id = buf.readIntLE()
  val name = buf.readString()
  val gameMode = buf.readUnsignedByte().toInt()
  val uniqueId = buf.readUUID()
  val generatorVersion = buf.readLongLE()

  val size = idOffset + buf.readableBytes()
  val data = byteBufAllocator.buffer(size)
  // Read data after the generator version or name
  buf.readBytes(data, idOffset, buf.readableBytes())
  val end = buf.readerIndex()
  // Read data before the id
  buf.readerIndex(0)
  buf.readBytes(data, 0, idOffset)
  // Move reader index back to the end
  buf.readerIndex(end)
  data.writerIndex(size)
  val eventInfoIndex = idOffset + generatorVersionToEventInfoOffset
  val eventInfo = data.getByte(eventInfoIndex).toInt()
  val serverSideCharacter = (eventInfo and 0x40) != 0
  if (serverSideCharacter) {
    // Clear the bit in the data
    data.setByte(eventInfoIndex, eventInfo and 0x40.inv())
  }
  WorldInfoPacket(id, uniqueId, name, generatorVersion, gameMode, serverSideCharacter, data)
}
