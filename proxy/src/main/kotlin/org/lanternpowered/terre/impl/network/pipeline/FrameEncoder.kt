/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.pipeline

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

internal class FrameEncoder : MessageToMessageEncoder<ByteBuf>() {

  override fun encode(ctx: ChannelHandlerContext, input: ByteBuf, output: MutableList<Any>) {
    // Allocate buffer just for the packet length
    val lengthBuf = ctx.alloc().buffer(Short.SIZE_BYTES)
    // Content length + length header
    lengthBuf.writeShortLE(input.readableBytes() + Short.SIZE_BYTES)

    output.add(lengthBuf)
    output.add(input.retain())
  }
}
