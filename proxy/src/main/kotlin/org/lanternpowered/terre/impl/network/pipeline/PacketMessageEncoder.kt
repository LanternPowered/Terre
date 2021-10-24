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
import io.netty.handler.codec.EncoderException
import io.netty.handler.codec.MessageToByteEncoder
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketCodecContext
import org.lanternpowered.terre.impl.network.UnknownPacket

internal class PacketMessageEncoder(
  private val context: PacketCodecContext
) : MessageToByteEncoder<Packet>() {

  override fun encode(ctx: ChannelHandlerContext, input: Packet, output: ByteBuf) {
    val result: ByteBuf
    val opcode: Int
    if (input is UnknownPacket) {
      result = input.data.retain()
      opcode = input.opcode
    } else {
      val registration = context.protocol.getEncoder(context.direction, input.javaClass)
        ?: throw EncoderException("No encoder is registered for packet type " +
          "${input::class.simpleName} with direction ${context.direction} for protocol " +
          "${context.protocol.name}.")

      opcode = registration.opcode
      result = try {
        registration.encoder.encode(context, input)
      } catch (ex: CodecException) {
        throw ex
      } catch (ex: Exception) {
        throw EncoderException("Error while encoding packet, type: $input opcode: $opcode", ex)
      }
    }

    try {
      if (opcode >= ModuleIdMask) { // module
        output.writeByte(ModulePacketId)
        output.writeShortLE(opcode ushr ModuleIdOffset)
      } else {
        output.writeByte(opcode)
      }
      output.writeBytes(result, result.readerIndex(), result.readableBytes())
    } finally {
      result.release()
    }
  }
}
