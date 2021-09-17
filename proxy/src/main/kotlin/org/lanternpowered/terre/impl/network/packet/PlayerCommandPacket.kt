/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class PlayerCommandPacket(
  val commandId: String,
  val arguments: String
) : Packet

internal val PlayerCommandEncoder = PacketEncoder<PlayerCommandPacket> { buf, packet ->
  buf.writeString(packet.commandId)
  buf.writeString(packet.arguments)
}

internal val PlayerCommandDecoder = PacketDecoder { buf ->
  val commandId = buf.readString()
  val arguments = buf.readString()
  PlayerCommandPacket(commandId, arguments)
}
