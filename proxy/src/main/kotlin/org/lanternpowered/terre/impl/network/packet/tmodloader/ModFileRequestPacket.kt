/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.tmodloader

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.ForwardingReferenceCounted
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class ModFileRequestPacket(val data: ByteBuf) :
  Packet, ForwardingReferenceCounted(data)

internal val ModFileRequestEncoder = PacketEncoder<ModFileRequestPacket> { buf, packet ->
  val data = packet.data
  buf.writeBytes(data, data.readerIndex(), data.readableBytes())
}

internal val ModFileRequestDecoder = PacketDecoder { buf ->
  val data = buf.readBytes(buf.readableBytes())
  ModFileRequestPacket(data)
}
