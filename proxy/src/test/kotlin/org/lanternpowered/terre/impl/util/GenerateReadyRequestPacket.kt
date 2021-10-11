/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util

import io.netty.buffer.Unpooled
import io.netty.handler.codec.base64.Base64
import org.junit.jupiter.api.Test
import org.lanternpowered.terre.impl.network.buffer.writeString

class GenerateReadyRequestPacket {

  /**
   * Generates a base64 encoded packet content that can be used to check if Terraria is running
   * and accepting connections. The server should respond with a version mismatch.
   */
  @Test fun generate() {
    val content = Unpooled.buffer()
    val framed = Unpooled.buffer()

    try {
      content.writeByte(0x01) // Connection request packet id
      content.writeString("Liveness") // Version string

      framed.writeShortLE(content.readableBytes() + Short.SIZE_BYTES)
      framed.writeBytes(content)

      println("Liveness: " + Base64.encode(framed).toString(Charsets.UTF_8))
    } finally {
      content.release()
      framed.release()
    }
  }
}
