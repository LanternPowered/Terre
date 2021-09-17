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
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.PacketDecoder

internal data class IsMobileResponsePacket(
  val isMobile: Boolean
) : Packet

internal val IsMobileResponseDecoder = PacketDecoder { buf ->
  val itemId = buf.readUnsignedShortLE()
  val playerId = buf.readPlayerId()

  if (itemId != IsMobileItemId)
    throw DecoderException("Unexpected item id: $itemId")

  when(playerId) {
    PlayerId.MobileNone -> IsMobileResponsePacket(true)
    PlayerId.None -> IsMobileResponsePacket(false)
    else -> throw DecoderException("Unexpected player id: $playerId")
  }
}
