/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.init

import org.lanternpowered.terre.impl.network.packet.DisconnectDecoder
import org.lanternpowered.terre.impl.network.packet.v155.Disconnect155Decoder
import org.lanternpowered.terre.impl.network.PacketDecoder

internal val DisconnectInitDecoder = PacketDecoder { buf ->
  try {
    val packet = DisconnectDecoder.decode(this, buf)
    if (buf.readableBytes() == 0)
      return@PacketDecoder packet
  } catch (ex: Exception) {
  }
  // Try again, but with the legacy protocol
  buf.readerIndex(0)
  Disconnect155Decoder.decode(this, buf)
}
