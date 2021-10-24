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
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class TileSquarePacket(
  val x: Int,
  val y: Int,
  val width: Int,
  val height: Int,
  val changeType: Int,
  val tiles: ByteBuf
) : Packet, ForwardingReferenceCounted(tiles)

internal val TileSquareEncoder = PacketEncoder<TileSquarePacket> { buf, packet ->
  buf.writeShortLE(packet.x)
  buf.writeShortLE(packet.y)
  buf.writeByte(packet.width)
  buf.writeByte(packet.height)
  buf.writeByte(packet.changeType)
  buf.writeBytes(packet.tiles)
}

internal val TileSquareDecoder = PacketDecoder { buf ->
  val x = buf.readShortLE().toInt()
  val y = buf.readShortLE().toInt()
  val width = buf.readByte().toInt()
  val height = buf.readByte().toInt()
  val changeType = buf.readByte().toInt()
  val tiles = buf.retainedSlice()
  TileSquarePacket(x, y, width, height, changeType, tiles)
}
