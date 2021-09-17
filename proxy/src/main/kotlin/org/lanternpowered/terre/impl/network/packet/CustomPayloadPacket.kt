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

/**
 * A packet reserved for custom communication between client and servers. This will be used for
 * custom communication between the proxy and backend servers.
 */
internal class CustomPayloadPacket(
  val content: ByteBuf
) : Packet, ForwardingReferenceCounted(content)

internal val CustomPayloadEncoder = PacketEncoder<CustomPayloadPacket> { buf, packet ->
  val content = packet.content
  buf.writeBytes(content, content.readerIndex(), content.readableBytes())
}

internal val CustomPayloadDecoder = PacketDecoder { buf ->
  val content = buf.readBytes(buf.readableBytes())
  CustomPayloadPacket(content)
}
