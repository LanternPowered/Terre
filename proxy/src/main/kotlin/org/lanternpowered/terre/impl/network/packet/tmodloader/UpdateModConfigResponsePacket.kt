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

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.readTaggedText
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.buffer.writeTaggedText
import org.lanternpowered.terre.text.Text

internal sealed interface UpdateModConfigResponsePacket : Packet {

  val mod: String

  data class Success(
    override val mod: String,
    val config: ModConfig,
  ) : UpdateModConfigResponsePacket

  data class Rejected(
    override val mod: String,
    val reason: Text,
  ) : UpdateModConfigResponsePacket
}

internal val UpdateModConfigResponseEncoder = PacketEncoder<UpdateModConfigResponsePacket> { buf, packet ->
  when (packet) {
    is UpdateModConfigResponsePacket.Success -> {
      buf.writeBoolean(true)
      buf.writeString(packet.mod)
      buf.writeString(packet.config.name)
      buf.writeString(packet.config.content)
    }
    is UpdateModConfigResponsePacket.Rejected -> {
      buf.writeBoolean(false)
      buf.writeString(packet.mod)
      buf.writeTaggedText(packet.reason)
    }
  }
}

internal val UpdateModConfigResponseDecoder = PacketDecoder { buf ->
  if (buf.readBoolean()) {
    val mod = buf.readString()
    val configName = buf.readString()
    val configContent = buf.readString()
    UpdateModConfigResponsePacket.Success(mod, ModConfig(configName, configContent))
  } else {
    val mod = buf.readString()
    val reason = buf.readTaggedText()
    UpdateModConfigResponsePacket.Rejected(mod, reason)
  }
}
