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
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class PlayerBuffsPacket(
  val playerId: PlayerId,
  val data: ByteBuf
) : Packet, ForwardingReferenceCounted(data)

internal val PlayerBuffsEncoder = PacketEncoder<PlayerBuffsPacket> { buf, packet ->
  val content = packet.data
  buf.writePlayerId(packet.playerId)
  buf.writeBytes(content, 0, content.readableBytes())
}

internal val PlayerBuffsDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val content = buf.readBytes(buf.readableBytes())
  PlayerBuffsPacket(playerId, content)
}
