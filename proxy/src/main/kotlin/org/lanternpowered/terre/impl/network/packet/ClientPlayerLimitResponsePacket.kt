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

import io.netty.handler.codec.DecoderException
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId

internal data class ClientPlayerLimitResponsePacket(
  val nonePlayerId: PlayerId
) : Packet

internal val ClientPlayerLimitResponseDecoder = PacketDecoder { buf ->
  val itemId = buf.readUnsignedShortLE()
  val nonePlayerId = buf.readPlayerId()

  if (itemId != ClientPlayerLimitItemId)
    throw DecoderException("Unexpected item id: $itemId")

  ClientPlayerLimitResponsePacket(nonePlayerId)
}
