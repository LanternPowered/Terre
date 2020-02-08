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
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal data class PlayerBuffsPacket(
    val playerId: PlayerId,
    val content: ByteBuf
) : ForwardingReferenceCounted(content), Packet

internal val PlayerBuffsEncoder = packetEncoderOf<PlayerBuffsPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeBytes(packet.content)
}

internal val PlayerBuffsDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val content = buf.readBytes(buf.readableBytes())
  PlayerBuffsPacket(playerId, content)
}
