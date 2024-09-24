/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.impl.network.packet.tmodloader

import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.packet.NpcUpdatePacket
import org.lanternpowered.terre.impl.network.packet.readNpcUpdate
import org.lanternpowered.terre.impl.network.packet.writeNpcUpdate

internal val NpcUpdateEncoder = PacketEncoder<NpcUpdatePacket> { buf, packet ->
  writeNpcUpdate(buf, packet, modded = true)
}

internal val NpcUpdateDecoder = PacketDecoder { buf ->
  readNpcUpdate(buf, modded = true)
}
