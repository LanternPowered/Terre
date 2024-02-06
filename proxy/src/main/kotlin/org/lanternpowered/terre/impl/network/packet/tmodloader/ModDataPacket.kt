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
import org.lanternpowered.terre.impl.network.PacketCodecContext
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class ModDataPacket(val data: ByteBuf) : Packet, ForwardingReferenceCounted(data)

internal val ModDataEncoder = object : PacketEncoder<ModDataPacket> {
  override fun encode(ctx: PacketCodecContext, packet: ModDataPacket): ByteBuf {
    return packet.data.retain()
  }
}

internal val ModDataDecoder = PacketDecoder { buf ->
  ModDataPacket(buf.retain())
}
