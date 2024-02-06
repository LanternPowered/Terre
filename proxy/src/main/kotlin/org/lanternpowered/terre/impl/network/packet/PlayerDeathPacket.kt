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

/**
 * A packet when a player dies.
 */
internal data class PlayerDeathPacket(
  val playerId: PlayerId,
  val data: ByteBuf,
) : Packet, ForwardingReferenceCounted(data)

internal val PlayerDeathEncoder = PacketEncoder<PlayerDeathPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeBytes(packet.data)
}

internal val PlayerDeathDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val data = buf.readBytes(buf.readableBytes())
  PlayerDeathPacket(playerId, data)
}
