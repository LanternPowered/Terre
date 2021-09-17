/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNUSED_PARAMETER", "FunctionName")

package org.lanternpowered.terre.impl.network

import io.netty.buffer.ByteBuf

internal inline fun <P : Packet> PacketEncoder(
  vararg `used named parameters`: Void,
  initialCapacity: Int,
  crossinline fn: PacketCodecContext.(buf: ByteBuf, packet: P) -> Unit
): PacketEncoder<P> {
  return object : PacketEncoder<P> {
    override fun encode(ctx: PacketCodecContext, packet: P): ByteBuf {
      val buf = ctx.byteBufAllocator.buffer(initialCapacity)
      fn(ctx, buf, packet)
      return buf
    }
  }
}

internal inline fun <P : Packet> PacketEncoder(
  crossinline fn: PacketCodecContext.(buf: ByteBuf, packet: P) -> Unit
): PacketEncoder<P> {
  return object : PacketEncoder<P> {
    override fun encode(ctx: PacketCodecContext, packet: P): ByteBuf {
      val buf = ctx.byteBufAllocator.buffer()
      fn(ctx, buf, packet)
      return buf
    }
  }
}

internal inline fun <P : Packet> PacketEncoder(
  crossinline fn: PacketCodecContext.(packet: P) -> ByteBuf
): PacketEncoder<P> {
  return object : PacketEncoder<P> {
    override fun encode(ctx: PacketCodecContext, packet: P): ByteBuf {
      return fn(ctx, packet)
    }
  }
}

internal interface PacketEncoder<P : Packet> {

  /**
   * Encodes the packet into a [ByteBuf].
   */
  fun encode(ctx: PacketCodecContext, packet: P): ByteBuf
}
