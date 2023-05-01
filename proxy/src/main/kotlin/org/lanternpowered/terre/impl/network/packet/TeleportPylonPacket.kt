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
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readShortVec2i
import org.lanternpowered.terre.impl.network.buffer.writeShortVec2i
import org.lanternpowered.terre.math.Vec2i

internal data class TeleportPylonPacket(
  val action: Action,
  val type: Int,
  val position: Vec2i,
) : Packet {

  enum class Action {
    Added,
    Removed,
    RequestTeleport,
  }
}

internal val TeleportPylonEncoder = PacketEncoder<TeleportPylonPacket> { buf, packet ->
  buf.writeByte(packet.action.ordinal)
  buf.writeShortVec2i(packet.position)
  buf.writeByte(packet.type)
}

internal val TeleportPylonDecoder = PacketDecoder { buf ->
  val action = TeleportPylonPacket.Action.entries[buf.readByte().toInt()]
  val position = buf.readShortVec2i()
  val type = buf.readByte().toInt()
  TeleportPylonPacket(action, type, position)
}
