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
import org.lanternpowered.terre.impl.network.buffer.writeString

internal data class UpdateModConfigRequestPacket(
  val mod: String,
  val config: ModConfig,
) : Packet

internal val UpdateModConfigRequestEncoder = PacketEncoder<UpdateModConfigRequestPacket> { buf, packet ->
  buf.writeString(packet.mod)
  buf.writeString(packet.config.name)
  buf.writeString(packet.config.content)
}

internal val UpdateModConfigRequestDecoder = PacketDecoder { buf ->
  val mod = buf.readString()
  val configName = buf.readString()
  val configContent = buf.readString()
  UpdateModConfigRequestPacket(mod, ModConfig(configName, configContent))
}
