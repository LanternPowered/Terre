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
import io.netty.handler.codec.CodecException
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.MessageToMessageDecoder
import org.lanternpowered.terre.impl.network.PacketCodecContext

internal class PacketMessageDecoder(private val context: PacketCodecContext) : MessageToMessageDecoder<ByteBuf>() {

  override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, output: MutableList<Any>) {
    val index = input.readerIndex()
    var opcode = input.readByte().toInt()
    // A net module packet, but we consider everything
    // a normal packet for simplicity, just with an id
    // offset.
    if (opcode == ModulePacketId) {
      opcode = input.readUnsignedShortLE() // Module id
      opcode = (opcode shl ModuleIdOffset) or ModuleIdMask
    }
    val registration = this.context.protocol.getDecoder(this.context.direction, opcode)

    // No registration is available, so just process
    // as an unknown packet.
    if (registration == null) {
      input.readerIndex(index)
      input.retain()
      output += input
      return
    }

    val content = input.retainedSlice()
    val packet = try {
      registration.decoder.decode(this.context, content)
    } catch (ex: CodecException) {
      throw ex
    } catch (ex: Exception) {
      throw DecoderException(ex)
    }
    if (packet != null)
      output += packet

    // Consume the bytes in the input, reading from the content
    // slice doesn't affect the input
    input.skipBytes(input.readableBytes())
  }
}
