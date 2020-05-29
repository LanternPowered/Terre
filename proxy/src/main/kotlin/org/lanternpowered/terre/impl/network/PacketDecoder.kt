/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import io.netty.buffer.ByteBuf

internal inline fun <P : Packet> packetDecoderOf(
    crossinline fn: PacketCodecContext.(buf: ByteBuf) -> P?): PacketDecoder<P> {
  return object : PacketDecoder<P> {
    override fun decode(ctx: PacketCodecContext, buf: ByteBuf) = fn(ctx, buf)
  }
}

internal interface PacketDecoder<P : Packet> {

  /**
   * Decodes a packet from the [ByteBuf].
   */
  fun decode(ctx: PacketCodecContext, buf: ByteBuf): P?
}
