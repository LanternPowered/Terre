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

import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readVec2i
import org.lanternpowered.terre.impl.network.buffer.writeVec2i
import org.lanternpowered.terre.impl.network.packet.WeaponsRackItemPacket

internal val WeaponsRackItemDecoder = PacketDecoder { buf ->
  val pos = buf.readVec2i()
  val itemStack = buf.readModdedItemStack()
  WeaponsRackItemPacket(pos, itemStack)
}

internal val WeaponsRackItemEncoder = PacketEncoder<WeaponsRackItemPacket> { buf, packet ->
  buf.writeVec2i(packet.pos)
  buf.writeModdedItemStack(packet.itemStack)
}
