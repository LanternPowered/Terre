package org.lanternpowered.terre.impl.network.packet.init

import org.lanternpowered.terre.impl.network.packet.DisconnectDecoder
import org.lanternpowered.terre.impl.network.packet.v155.Disconnect155Decoder
import org.lanternpowered.terre.impl.network.packetDecoderOf

internal val DisconnectInitDecoder = packetDecoderOf { buf ->
  try {
    DisconnectDecoder.decode(this, buf)
  } catch (ex: Exception) {
    buf.readerIndex(0)
    Disconnect155Decoder.decode(this, buf)
  }
}
