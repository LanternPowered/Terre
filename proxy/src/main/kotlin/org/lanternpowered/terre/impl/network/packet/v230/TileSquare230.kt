/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v230

import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.packet.TileSquarePacket

internal val TileSquare230Encoder = PacketEncoder<TileSquarePacket> { buf, packet ->
  check(packet.width == packet.height) { "Width and height must be the same" }
  var size = packet.width
  if (packet.changeType != 0)
    size += 0x8000
  buf.writeShortLE(size)
  if (packet.changeType != 0)
    buf.writeByte(packet.changeType)
  buf.writeShortLE(packet.x)
  buf.writeShortLE(packet.y)
  buf.writeBytes(packet.tiles)
}

internal val TileSquare230Decoder = PacketDecoder { buf ->
  val sizeAndFlag = buf.readUnsignedShortLE()
  val size = sizeAndFlag and 0x7fff
  val changeType = if (sizeAndFlag and 0x8000 != 0) {
    buf.readByte().toInt()
  } else 0
  val x = buf.readShortLE().toInt()
  val y = buf.readShortLE().toInt()
  val tiles = buf.retainedSlice()
  TileSquarePacket(x, y, size, size, changeType, tiles)
}
