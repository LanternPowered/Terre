/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v194

import org.lanternpowered.terre.impl.network.buffer.readNpcId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeNpcId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packet.UpdateNpcNamePacket
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal val UpdateNpcName194Encoder = PacketEncoder<UpdateNpcNamePacket> { buf, packet ->
  buf.writeNpcId(packet.npcId)
  buf.writeString(packet.name)
}

internal val UpdateNpcName194Decoder = PacketDecoder { buf ->
  val npcId = buf.readNpcId()
  val name = buf.readString()
  UpdateNpcNamePacket(npcId, name, 0)
}
