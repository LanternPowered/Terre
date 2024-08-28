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
import org.lanternpowered.terre.impl.network.buffer.readChestId
import org.lanternpowered.terre.impl.network.buffer.writeChestId
import org.lanternpowered.terre.impl.network.packet.ChestItemPacket

internal val ChestItemDecoder = PacketDecoder { buf ->
  val chestId = buf.readChestId()
  val slot = buf.readUnsignedByte().toInt()
  val itemStack = buf.readModdedItemStack()
  ChestItemPacket(chestId, slot, itemStack)
}

internal val ChestItemEncoder = PacketEncoder<ChestItemPacket> { buf, packet ->
  buf.writeChestId(packet.chestId)
  buf.writeByte(packet.slot)
  buf.writeModdedItemStack(packet.itemStack)
}
