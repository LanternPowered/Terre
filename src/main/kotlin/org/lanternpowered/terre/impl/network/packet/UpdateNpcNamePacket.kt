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

import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readNpcId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeNpcId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf

data class UpdateNpcNamePacket(
    val npcId: NpcId,
    val name: String
) : Packet

val UpdateNpcNameEncoder = packetEncoderOf<UpdateNpcNamePacket> { buf, packet ->
  buf.writeNpcId(packet.npcId)
  buf.writeString(packet.name)
}

val UpdateNpcNameDecoder = packetDecoderOf { buf ->
  val npcId = buf.readNpcId()
  val name = buf.readString()
  UpdateNpcNamePacket(npcId, name)
}
