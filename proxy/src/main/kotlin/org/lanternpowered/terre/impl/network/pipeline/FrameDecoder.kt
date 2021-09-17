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
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException

internal class FrameDecoder : ByteToMessageDecoder() {

  override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, output: MutableList<Any>) {
    if (!input.isReadable || input.readableBytes() < Short.SIZE_BYTES)
      return

    val index = input.readerIndex()
    val length = input.readUnsignedShortLE() - Short.SIZE_BYTES // Includes the header length

    if (length < 0)
      throw DecoderException("Invalid packet length: $length")

    // Check if all the bytes of the packet are available
    if (input.readableBytes() < length) {
      input.readerIndex(index)
      return
    }

    output.add(input.readRetainedSlice(length))
  }
}
